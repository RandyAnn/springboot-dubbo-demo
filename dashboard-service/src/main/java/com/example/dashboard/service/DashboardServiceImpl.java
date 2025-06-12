package com.example.dashboard.service;

import com.example.dashboard.command.DashboardStatsCommand;
import com.example.dashboard.command.NutritionTrendCommand;
import com.example.dashboard.command.PopularFoodsCommand;
import com.example.dashboard.dto.DashboardStatsDTO;
import com.example.dashboard.dto.NutritionTrendDTO;
import com.example.dashboard.dto.PopularFoodDTO;
import com.example.dashboard.service.DashboardService;
import com.example.diet.command.DietRecordQueryCommand;
import com.example.diet.dto.DietRecordQueryDTO;
import com.example.diet.dto.DietRecordResponseDTO;
import com.example.diet.service.DietRecordService;
import com.example.nutrition.service.NutritionStatService;
import com.example.shared.response.PageResult;
import com.example.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务实现类
 */
@Slf4j
@Service
@DubboService
public class DashboardServiceImpl implements DashboardService {

    @DubboReference
    private UserService userService;

    @DubboReference
    private DietRecordService dietRecordService;

    @DubboReference
    private NutritionStatService nutritionStatService;

    @Override
    public DashboardStatsDTO getDashboardStats(DashboardStatsCommand command) {
        log.debug("开始获取管理员仪表盘统计数据, command: {}", command);

        LocalDate queryDate = command.getDate() != null ? command.getDate() : LocalDate.now();
        DashboardStatsDTO stats = new DashboardStatsDTO();

        try {
            // 获取总用户数（直接查询，性能更好）
            long totalUsers = userService.getTotalUserCount();
            stats.setTotalUsers(totalUsers);
            log.debug("总用户数: {}", totalUsers);

            // 获取指定日期的饮食记录数
            int todayRecords = dietRecordService.countDietRecordsByDate(queryDate);
            stats.setTodayRecords(todayRecords);
            log.debug("{}饮食记录数: {}", queryDate, todayRecords);

            // 计算营养达标率
            double nutritionComplianceRate = nutritionStatService.calculateNutritionComplianceRate(queryDate);
            stats.setNutritionComplianceRate(nutritionComplianceRate);
            log.debug("营养达标率: {}%", nutritionComplianceRate);

            // 推荐准确率（模拟数据）
            stats.setRecommendationAccuracy(95);

            // 设置统计日期
            stats.setStatisticsDate(queryDate);

            log.debug("管理员仪表盘统计数据获取完成");
            return stats;

        } catch (Exception e) {
            log.error("获取管理员仪表盘统计数据失败", e);
            throw new RuntimeException("获取统计数据失败", e);
        }
    }

    @Override
    public NutritionTrendDTO getNutritionTrend(NutritionTrendCommand command) {
        log.debug("开始获取营养摄入趋势数据, command: {}", command);

        try {
            Map<String, Object> trendData = nutritionStatService.getAllNutritionTrend(command.getPeriod());

            // 转换为DTO
            NutritionTrendDTO dto = new NutritionTrendDTO();
            dto.setPeriod(command.getPeriod());
            dto.setDateList((List<String>) trendData.get("dateList"));
            dto.setCalorieList((List<Double>) trendData.get("calorieList"));
            dto.setProteinList((List<Double>) trendData.get("proteinList"));
            dto.setCarbsList((List<Double>) trendData.get("carbsList"));
            dto.setFatList((List<Double>) trendData.get("fatList"));
            dto.setDataPoints(dto.getDateList() != null ? dto.getDateList().size() : 0);

            log.debug("营养摄入趋势数据获取完成, period: {}", command.getPeriod());
            return dto;

        } catch (Exception e) {
            log.error("获取营养摄入趋势数据失败, command: {}", command, e);
            throw new RuntimeException("获取营养趋势数据失败", e);
        }
    }

    @Override
    public PageResult<DietRecordResponseDTO> getLatestDietRecords(DietRecordQueryDTO queryDTO) {
        log.debug("开始获取最新饮食记录列表, queryDTO: {}", queryDTO);

        try {
            // 构建查询命令对象
            DietRecordQueryCommand command = new DietRecordQueryCommand();
            BeanUtils.copyProperties(queryDTO, command);

            // 直接调用getAllUsersDietRecords，它会处理userId的逻辑
            PageResult<DietRecordResponseDTO> records = dietRecordService.getAllUsersDietRecords(command);

            log.debug("最新饮食记录列表获取完成, 记录数: {}", records.getTotal());
            return records;

        } catch (Exception e) {
            log.error("获取最新饮食记录列表失败, queryDTO: {}", queryDTO, e);
            throw new RuntimeException("获取饮食记录列表失败", e);
        }
    }

    @Override
    public DietRecordResponseDTO getDietRecordDetail(Long recordId) {
        log.debug("开始获取饮食记录详情, recordId: {}", recordId);

        try {
            DietRecordResponseDTO record = dietRecordService.getDietRecordDetail(recordId);

            if (record != null) {
                log.debug("饮食记录详情获取完成, recordId: {}", recordId);
            } else {
                log.warn("饮食记录不存在, recordId: {}", recordId);
            }

            return record;

        } catch (Exception e) {
            log.error("获取饮食记录详情失败, recordId: {}", recordId, e);
            throw new RuntimeException("获取饮食记录详情失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getPopularFoods(PopularFoodsCommand command) {
        log.debug("开始获取热门食物统计数据, command: {}", command);

        try {
            // 直接调用并返回结果
            List<Map<String, Object>> popularFoodsData = dietRecordService.getPopularFoodsByPeriod(
                    command.getPeriod(), command.getLimit());
            return popularFoodsData;

        } catch (Exception e) {
            log.error("获取热门食物统计数据失败, command: {}", command, e);
            throw new RuntimeException("获取热门食物数据失败", e);
        }
    }
}
