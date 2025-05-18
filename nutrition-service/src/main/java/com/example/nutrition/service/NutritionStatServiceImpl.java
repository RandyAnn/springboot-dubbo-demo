package com.example.nutrition.service;

import com.example.common.command.NutritionAdviceCommand;
import com.example.common.command.NutritionStatCommand;
import com.example.common.command.NutritionTrendCommand;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.NutritionAdviceDTO;
import com.example.common.dto.NutritionDetailItemDTO;
import com.example.common.dto.NutritionStatDTO;
import com.example.common.dto.NutritionTrendDTO;
import com.example.common.entity.DietRecord;
import com.example.common.entity.DietRecordFood;
import com.example.common.entity.NutritionAdvice;
import com.example.common.entity.UserNutritionGoal;
import com.example.common.service.DietRecordService;
import com.example.common.service.NutritionAdviceService;
import com.example.common.service.NutritionStatService;
import com.example.common.service.UserNutritionGoalService;
import com.example.nutrition.mapper.NutritionStatMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 营养统计服务实现类
 */
@Slf4j
@Service
@DubboService
public class NutritionStatServiceImpl implements NutritionStatService {

    @DubboReference
    private DietRecordService dietRecordService;

    @DubboReference
    private UserNutritionGoalService userNutritionGoalService;

    @Autowired
    private NutritionAdviceService nutritionAdviceService;

    private final NutritionStatMapper nutritionStatMapper;
    private final CacheService cacheService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long CACHE_EXPIRATION_HOURS = 24;
    private static final long CACHE_EXPIRATION_MINUTES = CACHE_EXPIRATION_HOURS * 60;
    private static final long CACHE_ADMIN_TREND_EXPIRATION_MINUTES = 30; // 管理员趋势数据缓存30分钟

    @Autowired
    public NutritionStatServiceImpl(NutritionStatMapper nutritionStatMapper,
                                    CacheService cacheService) {
        this.nutritionStatMapper = nutritionStatMapper;
        this.cacheService = cacheService;
    }

    @Override
    public NutritionStatDTO getDailyNutritionStat(NutritionStatCommand command) {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();
        // 构建缓存键
        String cacheKey = "daily:" + userId + ":" + date;

        // 尝试从缓存获取
        NutritionStatDTO cachedStat = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedStat != null) {
            return cachedStat;
        }

        // 查询用户营养目标
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getNutritionGoalByUserId(userId);
        if (nutritionGoal == null) {
            // 如果用户没有设置营养目标，使用默认值
            nutritionGoal = createDefaultNutritionGoal(userId);
        }

        // 从数据库获取当日所有饮食记录
        List<DietRecord> dietRecords = nutritionStatMapper.findDietRecordsByUserIdAndDate(userId, date);

        // 初始化营养统计数据
        NutritionStatDTO nutritionStat = new NutritionStatDTO();
        nutritionStat.setDate(date.format(DATE_FORMATTER));
        nutritionStat.setCalorie(0);
        nutritionStat.setProtein(0.0);
        nutritionStat.setCarbs(0.0);
        nutritionStat.setFat(0.0);

        // 汇总当日营养数据
        for (DietRecord dietRecord : dietRecords) {
            List<DietRecordFood> foods = nutritionStatMapper.findDietRecordFoodsByRecordId(dietRecord.getId());
            for (DietRecordFood food : foods) {
                nutritionStat.setCalorie(nutritionStat.getCalorie() + (food.getCalories() != null ? food.getCalories().intValue() : 0));
                nutritionStat.setProtein(nutritionStat.getProtein() + (food.getProtein() != null ? food.getProtein().doubleValue() : 0));
                nutritionStat.setCarbs(nutritionStat.getCarbs() + (food.getCarbs() != null ? food.getCarbs().doubleValue() : 0));
                nutritionStat.setFat(nutritionStat.getFat() + (food.getFat() != null ? food.getFat().doubleValue() : 0));
            }
        }

        // 计算目标达成百分比
        if (nutritionGoal.getCalorieTarget() != null && nutritionGoal.getCalorieTarget() > 0) {
            nutritionStat.setCaloriePercentage(nutritionStat.getCalorie() * 100.0 / nutritionGoal.getCalorieTarget());
        } else {
            nutritionStat.setCaloriePercentage(0.0);
        }

        if (nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() > 0) {
            nutritionStat.setProteinPercentage(nutritionStat.getProtein() * 100.0 / nutritionGoal.getProteinTarget());
        } else {
            nutritionStat.setProteinPercentage(0.0);
        }

        if (nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() > 0) {
            nutritionStat.setCarbsPercentage(nutritionStat.getCarbs() * 100.0 / nutritionGoal.getCarbsTarget());
        } else {
            nutritionStat.setCarbsPercentage(0.0);
        }

        if (nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() > 0) {
            nutritionStat.setFatPercentage(nutritionStat.getFat() * 100.0 / nutritionGoal.getFatTarget());
        } else {
            nutritionStat.setFatPercentage(0.0);
        }

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, nutritionStat, CACHE_EXPIRATION_MINUTES);

        return nutritionStat;
    }



    @Override
    public NutritionTrendDTO getNutritionTrend(NutritionTrendCommand command) {
        Long userId = command.getUserId();
        LocalDate startDate = command.getStartDate();
        LocalDate endDate = command.getEndDate();
        // 构建缓存键
        String cacheKey = "trend:" + userId + ":" + startDate + "-" + endDate;

        // 尝试从缓存获取
        NutritionTrendDTO cachedTrend = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedTrend != null) {
            return cachedTrend;
        }

        NutritionTrendDTO trendDTO = new NutritionTrendDTO();
        List<String> dateList = new ArrayList<>();
        List<Integer> calorieList = new ArrayList<>();
        List<Double> proteinList = new ArrayList<>();
        List<Double> carbsList = new ArrayList<>();
        List<Double> fatList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 获取每日营养数据
            NutritionStatDTO dailyStat = getDailyNutritionStat(NutritionStatCommand.of(userId, currentDate));

            // 添加到趋势数据
            dateList.add(currentDate.format(DATE_FORMATTER));
            calorieList.add(dailyStat.getCalorie());
            proteinList.add(dailyStat.getProtein());
            carbsList.add(dailyStat.getCarbs());
            fatList.add(dailyStat.getFat());

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }

        trendDTO.setDateList(dateList);
        trendDTO.setCalorieList(calorieList);
        trendDTO.setProteinList(proteinList);
        trendDTO.setCarbsList(carbsList);
        trendDTO.setFatList(fatList);

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, trendDTO, CACHE_EXPIRATION_MINUTES);

        return trendDTO;
    }



    @Override
    public List<NutritionDetailItemDTO> getNutritionDetails(NutritionStatCommand command) {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();
        // 构建缓存键
        String cacheKey = "details:" + userId + ":" + date;

        // 尝试从缓存获取
        List<NutritionDetailItemDTO> cachedDetails = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedDetails != null) {
            return cachedDetails;
        }

        // 获取当日营养摄入统计
        NutritionStatDTO nutritionStat = getDailyNutritionStat(NutritionStatCommand.of(userId, date));

        // 获取用户营养目标
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getNutritionGoalByUserId(userId);
        if (nutritionGoal == null) {
            nutritionGoal = createDefaultNutritionGoal(userId);
        }

        List<NutritionDetailItemDTO> detailList = new ArrayList<>();

        // 添加蛋白质详情
        double proteinPercentage = nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() > 0
                ? Math.min(100, nutritionStat.getProtein() * 100.0 / nutritionGoal.getProteinTarget())
                : 0.0;
        detailList.add(new NutritionDetailItemDTO("蛋白质", nutritionStat.getProtein(), "g", proteinPercentage));

        // 添加碳水化合物详情
        double carbsPercentage = nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() > 0
                ? Math.min(100, nutritionStat.getCarbs() * 100.0 / nutritionGoal.getCarbsTarget())
                : 0.0;
        detailList.add(new NutritionDetailItemDTO("碳水化合物", nutritionStat.getCarbs(), "g", carbsPercentage));

        // 添加脂肪详情
        double fatPercentage = nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() > 0
                ? Math.min(100, nutritionStat.getFat() * 100.0 / nutritionGoal.getFatTarget())
                : 0.0;
        detailList.add(new NutritionDetailItemDTO("脂肪", nutritionStat.getFat(), "g", fatPercentage));

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, detailList, CACHE_EXPIRATION_MINUTES);

        return detailList;
    }



    @Override
    public List<NutritionAdviceDTO> getNutritionAdvice(NutritionAdviceCommand command) {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();
        // 构建缓存键
        String cacheKey = "advice:" + userId + ":" + date;

        // 尝试从缓存获取
        List<NutritionAdviceDTO> cachedAdvice = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedAdvice != null) {
            return cachedAdvice;
        }

        List<NutritionAdviceDTO> adviceList = new ArrayList<>();

        // 获取当日营养摄入统计
        NutritionStatDTO nutritionStat = getDailyNutritionStat(NutritionStatCommand.of(userId, date));

        // 获取用户营养目标
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getNutritionGoalByUserId(userId);
        if (nutritionGoal == null) {
            nutritionGoal = createDefaultNutritionGoal(userId);
        }

        // 根据与目标的差距生成建议

        // 检查蛋白质摄入
        NutritionAdvice proteinAdvice = nutritionAdviceService.getAdviceByCondition("protein", nutritionStat.getProteinPercentage().intValue());
        if (proteinAdvice != null) {
            adviceList.add(nutritionAdviceService.convertToDTO(proteinAdvice));
        }

        // 检查碳水化合物摄入
        NutritionAdvice carbsAdvice = nutritionAdviceService.getAdviceByCondition("carbs", nutritionStat.getCarbsPercentage().intValue());
        if (carbsAdvice != null) {
            adviceList.add(nutritionAdviceService.convertToDTO(carbsAdvice));
        }

        // 检查脂肪摄入
        NutritionAdvice fatAdvice = nutritionAdviceService.getAdviceByCondition("fat", nutritionStat.getFatPercentage().intValue());
        if (fatAdvice != null) {
            adviceList.add(nutritionAdviceService.convertToDTO(fatAdvice));
        }

        // 检查热量摄入
        NutritionAdvice calorieAdvice = nutritionAdviceService.getAdviceByCondition("calorie", nutritionStat.getCaloriePercentage().intValue());
        if (calorieAdvice != null) {
            adviceList.add(nutritionAdviceService.convertToDTO(calorieAdvice));
        }

        // 如果没有任何建议，添加一个默认建议
        if (adviceList.isEmpty()) {
            NutritionAdvice defaultAdvice = nutritionAdviceService.getDefaultAdvice();
            if (defaultAdvice != null) {
                adviceList.add(nutritionAdviceService.convertToDTO(defaultAdvice));
            } else {
                // 如果数据库中没有默认建议，使用硬编码的默认建议
                adviceList.add(new NutritionAdviceDTO(
                        "info",
                        "营养摄入基本合理",
                        "今日的营养摄入基本合理，保持均衡饮食有助于健康。"
                ));
            }
        }

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, adviceList, CACHE_EXPIRATION_MINUTES);

        return adviceList;
    }



    /**
     * 创建默认的营养目标
     * @param userId 用户ID
     * @return 默认营养目标
     */
    private UserNutritionGoal createDefaultNutritionGoal(Long userId) {
        UserNutritionGoal goal = new UserNutritionGoal();
        goal.setUserId(userId);
        goal.setCalorieTarget(2200);      // 默认每日2200卡路里
        goal.setProteinTarget(65);        // 默认每日65克蛋白质
        goal.setCarbsTarget(300);         // 默认每日300克碳水
        goal.setFatTarget(70);            // 默认每日70克脂肪
        goal.setIsVegetarian(false);
        goal.setIsLowCarb(false);
        goal.setIsHighProtein(false);
        goal.setIsGlutenFree(false);
        goal.setIsLowSodium(false);
        return goal;
    }

    @Override
    public double calculateNutritionComplianceRate(LocalDate date) {
        // 构建缓存键
        String cacheKey = "compliance:" + date;

        // 尝试从缓存获取
        Double cachedRate = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedRate != null) {
            return cachedRate;
        }

        // 获取所有活跃用户ID列表
        List<Long> activeUserIds = dietRecordService.findActiveUserIdsByDate(date);

        if (activeUserIds.isEmpty()) {
            return 0.0; // 如果没有活跃用户，返回0
        }

        int compliantUsers = 0;

        // 遍历所有活跃用户，检查他们的营养达标情况
        for (Long userId : activeUserIds) {
            // 获取用户当日营养数据，使用不缓存的方法
            NutritionStatDTO nutritionStat = getDailyNutritionStatNoCache(userId, date);

            // 获取用户营养目标
            UserNutritionGoal nutritionGoal = userNutritionGoalService.getNutritionGoalByUserId(userId);
            if (nutritionGoal == null) {
                nutritionGoal = createDefaultNutritionGoal(userId);
            }

            // 检查是否达标（这里简化为热量、蛋白质、碳水和脂肪都达到目标的80%以上）
            boolean isCompliant =
                nutritionStat.getCaloriePercentage() >= 80 &&
                nutritionStat.getProteinPercentage() >= 80 &&
                nutritionStat.getCarbsPercentage() >= 80 &&
                nutritionStat.getFatPercentage() >= 80;

            if (isCompliant) {
                compliantUsers++;
            }
        }

        // 计算达标率
        double complianceRate = (double) compliantUsers / activeUserIds.size() * 100;

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, complianceRate, CACHE_EXPIRATION_MINUTES);

        return complianceRate;
    }



    @Override
    public Map<String, Object> getAdminNutritionTrend(String period, LocalDate startDate, LocalDate endDate) {
        // 处理日期参数
        LocalDate today = LocalDate.now();

        // 根据period设置默认的日期范围
        if (startDate == null) {
            switch (period) {
                case "week":
                    startDate = today.minus(6, ChronoUnit.DAYS); // 最近一周
                    break;
                case "month":
                    startDate = today.minus(29, ChronoUnit.DAYS); // 最近一个月
                    break;
                case "year":
                    startDate = today.minus(364, ChronoUnit.DAYS); // 最近一年
                    break;
                default:
                    startDate = today.minus(29, ChronoUnit.DAYS); // 默认一个月
            }
        }

        if (endDate == null) {
            endDate = today;
        }

        // 限制日期范围，防止查询过大的数据量
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 365) { // 最多查询一年的数据
            startDate = endDate.minus(365, ChronoUnit.DAYS);
        }

        // 构建缓存键
        String cacheKey = "admin:trend:" + period + ":"
                + startDate.format(DATE_FORMATTER) + ":"
                + endDate.format(DATE_FORMATTER);

        // 尝试从缓存获取
        Map<String, Object> cachedTrend = cacheService.get(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey);
        if (cachedTrend != null) {
            return cachedTrend;
        }

        // 缓存未命中，计算趋势数据
        Map<String, Object> trendData = calculateAverageNutritionTrend(startDate, endDate);

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.NUTRITION_STATS_CACHE, cacheKey, trendData, CACHE_ADMIN_TREND_EXPIRATION_MINUTES);

        return trendData;
    }



    /**
     * 计算所有用户的平均营养摄入趋势
     *
     * 注意：这里使用getDailyNutritionStatNoCache方法而不是getDailyNutritionStat方法
     * 原因是避免为管理员仪表盘创建大量用户个人的Redis缓存
     * 如果使用getDailyNutritionStat，会为每个用户每天创建一个Redis缓存
     * 当用户数量很多且查询时间跨度大时，会在Redis中创建大量不必要的缓存
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含趋势数据的Map
     */
    private Map<String, Object> calculateAverageNutritionTrend(LocalDate startDate, LocalDate endDate) {
        // 获取所有活跃用户ID列表
        List<Long> activeUserIds = dietRecordService.findActiveUserIdsByDateRange(startDate, endDate);

        if (activeUserIds.isEmpty()) {
            // 如果没有活跃用户，返回空数据
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("dateList", new ArrayList<String>());
            emptyResult.put("calorieList", new ArrayList<Integer>());
            emptyResult.put("proteinList", new ArrayList<Double>());
            emptyResult.put("carbsList", new ArrayList<Double>());
            emptyResult.put("fatList", new ArrayList<Double>());
            return emptyResult;
        }

        // 准备结果数据结构
        List<String> dateList = new ArrayList<>();
        List<Double> calorieList = new ArrayList<>();
        List<Double> proteinList = new ArrayList<>();
        List<Double> carbsList = new ArrayList<>();
        List<Double> fatList = new ArrayList<>();

        // 遍历日期范围
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 添加日期
            dateList.add(currentDate.format(DATE_FORMATTER));

            // 计算当日所有用户的平均营养摄入
            double totalCalorie = 0;
            double totalProtein = 0;
            double totalCarbs = 0;
            double totalFat = 0;
            int userCount = 0;

            for (Long userId : activeUserIds) {
                try {
                    // 直接从数据库获取用户营养数据，而不是调用创建缓存的getDailyNutritionStat方法
                    NutritionStatDTO nutritionStat = getDailyNutritionStatNoCache(userId, currentDate);

                    // 累加营养数据
                    totalCalorie += nutritionStat.getCalorie();
                    totalProtein += nutritionStat.getProtein();
                    totalCarbs += nutritionStat.getCarbs();
                    totalFat += nutritionStat.getFat();
                    userCount++;
                } catch (Exception e) {
                    // 忽略单个用户的错误，继续处理其他用户
                    log.error("获取用户营养数据失败: userId={}, date={}", userId, currentDate, e);
                }
            }

            // 计算平均值
            if (userCount > 0) {
                calorieList.add(totalCalorie / userCount);
                proteinList.add(totalProtein / userCount);
                carbsList.add(totalCarbs / userCount);
                fatList.add(totalFat / userCount);
            } else {
                // 如果当天没有数据，添加0
                calorieList.add(0.0);
                proteinList.add(0.0);
                carbsList.add(0.0);
                fatList.add(0.0);
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("calorieList", calorieList);
        result.put("proteinList", proteinList);
        result.put("carbsList", carbsList);
        result.put("fatList", fatList);

        return result;
    }

    /**
     * 获取用户每日营养数据（不缓存）
     * 仅用于管理员查看整体趋势，避免为每个用户创建大量缓存
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 营养统计数据
     */
    private NutritionStatDTO getDailyNutritionStatNoCache(Long userId, LocalDate date) {
        // 查询用户营养目标
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getNutritionGoalByUserId(userId);
        if (nutritionGoal == null) {
            // 如果用户没有设置营养目标，使用默认值
            nutritionGoal = createDefaultNutritionGoal(userId);
        }

        // 从数据库获取当日所有饮食记录
        List<DietRecord> dietRecords = nutritionStatMapper.findDietRecordsByUserIdAndDate(userId, date);

        // 初始化营养统计数据
        NutritionStatDTO nutritionStat = new NutritionStatDTO();
        nutritionStat.setDate(date.format(DATE_FORMATTER));
        nutritionStat.setCalorie(0);
        nutritionStat.setProtein(0.0);
        nutritionStat.setCarbs(0.0);
        nutritionStat.setFat(0.0);

        // 汇总当日营养数据
        for (DietRecord dietRecord : dietRecords) {
            List<DietRecordFood> foods = nutritionStatMapper.findDietRecordFoodsByRecordId(dietRecord.getId());
            for (DietRecordFood food : foods) {
                nutritionStat.setCalorie(nutritionStat.getCalorie() + (food.getCalories() != null ? food.getCalories().intValue() : 0));
                nutritionStat.setProtein(nutritionStat.getProtein() + (food.getProtein() != null ? food.getProtein().doubleValue() : 0));
                nutritionStat.setCarbs(nutritionStat.getCarbs() + (food.getCarbs() != null ? food.getCarbs().doubleValue() : 0));
                nutritionStat.setFat(nutritionStat.getFat() + (food.getFat() != null ? food.getFat().doubleValue() : 0));
            }
        }

        // 计算目标达成百分比
        if (nutritionGoal.getCalorieTarget() != null && nutritionGoal.getCalorieTarget() > 0) {
            nutritionStat.setCaloriePercentage(nutritionStat.getCalorie() * 100.0 / nutritionGoal.getCalorieTarget());
        } else {
            nutritionStat.setCaloriePercentage(0.0);
        }

        if (nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() > 0) {
            nutritionStat.setProteinPercentage(nutritionStat.getProtein() * 100.0 / nutritionGoal.getProteinTarget());
        } else {
            nutritionStat.setProteinPercentage(0.0);
        }

        if (nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() > 0) {
            nutritionStat.setCarbsPercentage(nutritionStat.getCarbs() * 100.0 / nutritionGoal.getCarbsTarget());
        } else {
            nutritionStat.setCarbsPercentage(0.0);
        }

        if (nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() > 0) {
            nutritionStat.setFatPercentage(nutritionStat.getFat() * 100.0 / nutritionGoal.getFatTarget());
        } else {
            nutritionStat.setFatPercentage(0.0);
        }

        return nutritionStat;
    }
}