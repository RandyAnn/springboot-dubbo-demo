package com.example.nutrition.service;

import com.example.common.command.NutritionStatCommand;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.*;
import com.example.common.entity.UserNutritionGoal;
import org.springframework.beans.BeanUtils;
import com.example.common.service.HealthReportService;
import com.example.common.service.NutritionStatService;
import com.example.common.service.UserNutritionGoalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 健康报告服务实现类
 */
@Slf4j
@Service
@DubboService
public class HealthReportServiceImpl implements HealthReportService {

    @DubboReference
    private UserNutritionGoalService userNutritionGoalService;

    @Autowired
    private NutritionStatService nutritionStatService;

    private final CacheService cacheService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long CACHE_EXPIRATION_HOURS = 24;
    private static final long CACHE_EXPIRATION_MINUTES = CACHE_EXPIRATION_HOURS * 60;

    @Autowired
    public HealthReportServiceImpl(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public HealthReportDTO getHealthReport(Long userId, LocalDate date) {
        // 构建缓存键
        String cacheKey = "report:" + userId + ":" + date;

        // 尝试从缓存获取
        HealthReportDTO cachedReport = cacheService.get(CommonCacheConfig.HEALTH_REPORT_CACHE, cacheKey);
        if (cachedReport != null) {
            log.debug("从缓存获取健康报告: userId={}, date={}", userId, date);
            return cachedReport;
        }

        UserNutritionGoalResponseDTO nutritionGoalDTO = userNutritionGoalService.getNutritionGoal(userId);
        UserNutritionGoal nutritionGoal = convertDTOToEntity(nutritionGoalDTO);

        // 获取当前日期的营养统计数据
        NutritionStatDTO currentNutritionStat = nutritionStatService.getDailyNutritionStat(
                NutritionStatCommand.builder().userId(userId).date(date).build());

        // 计算上周同一天的日期
        LocalDate lastWeekDate = date.minus(7, ChronoUnit.DAYS);

        // 获取上周同一天的营养统计数据
        NutritionStatDTO lastWeekNutritionStat = nutritionStatService.getDailyNutritionStat(
                NutritionStatCommand.builder().userId(userId).date(lastWeekDate).build());

        // 构建健康报告
        HealthReportDTO healthReport = new HealthReportDTO();

        // 计算健康分数
        int healthScore = calculateHealthScore(currentNutritionStat, nutritionGoal);
        healthReport.setHealthScore(healthScore);

        // 计算分数变化
        int lastWeekHealthScore = calculateHealthScore(lastWeekNutritionStat, nutritionGoal);
        healthReport.setScoreChange(healthScore - lastWeekHealthScore);

        // 生成营养平衡数据
        NutritionBalanceDTO nutritionBalance = calculateNutritionBalance(currentNutritionStat, nutritionGoal);
        healthReport.setNutritionBalance(nutritionBalance);

        // 生成周进度对比数据
        WeeklyProgressDTO weeklyProgress = calculateWeeklyProgress(currentNutritionStat, lastWeekNutritionStat);
        healthReport.setWeeklyProgress(weeklyProgress);

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.HEALTH_REPORT_CACHE, cacheKey, healthReport, CACHE_EXPIRATION_MINUTES);

        return healthReport;
    }

    /**
     * 计算健康分数
     * @param nutritionStat 营养统计数据
     * @param nutritionGoal 营养目标
     * @return 健康分数（0-100）
     */
    private int calculateHealthScore(NutritionStatDTO nutritionStat, UserNutritionGoal nutritionGoal) {
        if (nutritionStat == null) {
            return 50; // 默认分数
        }

        // 计算卡路里得分（超标或不足都会扣分）
        double calorieScore = nutritionStat.getCaloriePercentage() > 100
            ? 100 - (nutritionStat.getCaloriePercentage() - 100) * 0.5
            : nutritionStat.getCaloriePercentage();
        calorieScore = Math.max(0, calorieScore);

        // 计算蛋白质得分
        double proteinScore = nutritionStat.getProteinPercentage() > 150
            ? 70
            : (nutritionStat.getProteinPercentage() / 100) * 100;
        proteinScore = Math.max(0, proteinScore);

        // 计算碳水得分
        double carbsScore = nutritionStat.getCarbsPercentage() > 150
            ? 60
            : (nutritionStat.getCarbsPercentage() / 100) * 100;
        carbsScore = Math.max(0, carbsScore);

        // 计算脂肪得分
        double fatScore = nutritionStat.getFatPercentage() > 150
            ? 50
            : (nutritionStat.getFatPercentage() / 100) * 100;
        fatScore = Math.max(0, fatScore);

        // 总体健康分数（不同指标权重不同）
        double totalScore = calorieScore * 0.4 + proteinScore * 0.3 + carbsScore * 0.2 + fatScore * 0.1;

        return Math.min(100, (int) Math.round(totalScore));
    }

    /**
     * 计算营养平衡数据
     * @param nutritionStat 营养统计数据
     * @param nutritionGoal 营养目标
     * @return 营养平衡数据
     */
    private NutritionBalanceDTO calculateNutritionBalance(NutritionStatDTO nutritionStat, UserNutritionGoal nutritionGoal) {
        if (nutritionStat == null) {
            return createDefaultNutritionBalance();
        }

        // 计算各项营养素达成百分比，最高150%
        int proteinPercentage = (int) Math.min(150, nutritionStat.getProteinPercentage());
        int carbsPercentage = (int) Math.min(150, nutritionStat.getCarbsPercentage());
        int fatPercentage = (int) Math.min(150, nutritionStat.getFatPercentage());

        return NutritionBalanceDTO.builder()
                .protein(proteinPercentage)
                .carbs(carbsPercentage)
                .fat(fatPercentage)
                .build();
    }

    /**
     * 计算周进度对比数据
     * @param currentStat 当前营养统计
     * @param lastWeekStat 上周营养统计
     * @return 周进度对比数据
     */
    private WeeklyProgressDTO calculateWeeklyProgress(NutritionStatDTO currentStat, NutritionStatDTO lastWeekStat) {
        if (currentStat == null) {
            currentStat = new NutritionStatDTO();
        }

        if (lastWeekStat == null) {
            lastWeekStat = new NutritionStatDTO();
        }

        // 创建热量对比
        NutrientProgressDTO calorieProgress = NutrientProgressDTO.builder()
                .lastWeek((double) lastWeekStat.getCalorie())
                .thisWeek((double) currentStat.getCalorie())
                .build();

        // 创建蛋白质对比
        NutrientProgressDTO proteinProgress = NutrientProgressDTO.builder()
                .lastWeek(lastWeekStat.getProtein())
                .thisWeek(currentStat.getProtein())
                .build();

        // 创建碳水对比
        NutrientProgressDTO carbsProgress = NutrientProgressDTO.builder()
                .lastWeek(lastWeekStat.getCarbs())
                .thisWeek(currentStat.getCarbs())
                .build();

        // 创建脂肪对比
        NutrientProgressDTO fatProgress = NutrientProgressDTO.builder()
                .lastWeek(lastWeekStat.getFat())
                .thisWeek(currentStat.getFat())
                .build();

        return WeeklyProgressDTO.builder()
                .calorie(calorieProgress)
                .protein(proteinProgress)
                .carbs(carbsProgress)
                .fat(fatProgress)
                .build();
    }

    /**
     * 创建默认营养平衡数据
     * @return 默认营养平衡数据
     */
    private NutritionBalanceDTO createDefaultNutritionBalance() {
        return NutritionBalanceDTO.builder()
                .protein(0)
                .carbs(0)
                .fat(0)
                .build();
    }

    /**
     * 将UserNutritionGoalResponseDTO转换为UserNutritionGoal实体
     */
    private UserNutritionGoal convertDTOToEntity(UserNutritionGoalResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        UserNutritionGoal entity = new UserNutritionGoal();
        BeanUtils.copyProperties(dto, entity);

        // 转换LocalDateTime到Date
        if (dto.getCreatedAt() != null) {
            entity.setCreatedAt(java.sql.Timestamp.valueOf(dto.getCreatedAt()));
        }
        if (dto.getUpdatedAt() != null) {
            entity.setUpdatedAt(java.sql.Timestamp.valueOf(dto.getUpdatedAt()));
        }

        return entity;
    }
}