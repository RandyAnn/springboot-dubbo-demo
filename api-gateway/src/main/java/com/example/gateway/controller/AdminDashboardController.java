package com.example.gateway.controller;

import com.example.common.command.DietRecordQueryCommand;
import com.example.common.dto.*;
import com.example.common.entity.User;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.DietRecordService;
import com.example.common.service.NutritionStatService;
import com.example.common.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员仪表盘控制器
 * 提供管理员仪表盘相关的API接口
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @DubboReference
    private UserService userService;

    @DubboReference
    private DietRecordService dietRecordService;

    @DubboReference
    private NutritionStatService nutritionStatService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DASHBOARD_STATS_CACHE_KEY = "admin:dashboard:stats";
    private static final String NUTRITION_TREND_CACHE_KEY = "admin:dashboard:nutrition_trend";
    private static final String POPULAR_FOODS_CACHE_KEY = "admin:dashboard:popular_foods";
    private static final long DASHBOARD_STATS_CACHE_TTL = 5; // 缓存5分钟
    private static final long NUTRITION_TREND_CACHE_TTL = 30; // 缓存30分钟
    private static final long POPULAR_FOODS_CACHE_TTL = 30; // 缓存30分钟

    /**
     * 获取管理员仪表盘统计数据
     * 包括：总用户数、今日饮食记录数、营养达标率等
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedStats = (Map<String, Object>) redisTemplate.opsForValue().get(DASHBOARD_STATS_CACHE_KEY);

        if (cachedStats != null) {
            return ResponseEntity.ok(ApiResponse.success(cachedStats));
        }

        // 缓存未命中，从数据库获取
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取总用户数
            PageResult<UserInfoDTO> userPage = userService.getUserInfoPage(1, 1, null, null, null);
            stats.put("totalUsers", userPage.getTotal());

            // 获取今日饮食记录数
            LocalDate today = LocalDate.now();
            int todayRecords = dietRecordService.countDietRecordsByDate(today);
            stats.put("todayRecords", todayRecords);

            // 计算营养达标率
            // 这里简化处理，获取所有用户当日的营养达标情况，计算达标率
            double nutritionComplianceRate = nutritionStatService.calculateNutritionComplianceRate(today);
            stats.put("nutritionComplianceRate", nutritionComplianceRate);

            // 推荐准确率（这里使用模拟数据）
            stats.put("recommendationAccuracy", 95);

            // 将结果存入缓存，设置过期时间
            redisTemplate.opsForValue().set(DASHBOARD_STATS_CACHE_KEY, stats, DASHBOARD_STATS_CACHE_TTL, TimeUnit.MINUTES);

            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            // 记录错误并返回空结果
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.success(stats));
        }
    }

    /**
     * 获取用户营养摄入趋势数据
     * 用于管理员仪表盘展示所有用户的平均营养摄入趋势
     *
     * @param period 时间周期：week(周)、month(月)、year(年)，默认为month
     * @param startDate 开始日期，格式：yyyy-MM-dd，默认根据period自动计算
     * @param endDate 结束日期，格式：yyyy-MM-dd，默认为当天
     * @return 营养摄入趋势数据
     */
    @GetMapping("/nutrition-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNutritionTrend(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        try {
            Map<String, Object> trendData = nutritionStatService.getAdminNutritionTrend(period, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(trendData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "获取营养趋势数据失败"));
        }
    }

    /**
     * 获取最新饮食记录列表
     * 用于管理员仪表盘展示所有用户的最新饮食记录
     *
     * @param page 页码，默认为1
     * @param size 每页记录数，默认为10
     * @param mealType 餐次类型，可选参数：早餐、午餐、晚餐、加餐
     * @param startDate 开始日期，格式：yyyy-MM-dd
     * @param endDate 结束日期，格式：yyyy-MM-dd
     * @param userId 用户ID，可选参数，用于筛选特定用户的记录
     * @return 饮食记录列表
     */
    @GetMapping("/latest-diet-records")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<DietRecordResponseDTO>>> getLatestDietRecords(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long userId) {

        try {
            // 构建查询命令对象
            DietRecordQueryCommand command = new DietRecordQueryCommand();
            command.setPage(page);
            command.setSize(size);
            command.setMealType(mealType);
            command.setUserId(userId);

            if (startDate != null) {
                command.setStartDate(startDate.toString());
            }

            if (endDate != null) {
                command.setEndDate(endDate.toString());
            }

            // 调用服务获取饮食记录
            PageResult<DietRecordResponseDTO> records;

            if (userId != null) {
                // 查询特定用户的记录
                records = dietRecordService.getDietRecords(command);
            } else {
                // 查询所有用户的记录
                records = dietRecordService.getAllUsersDietRecords(command);
            }

            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "获取最新饮食记录失败"));
        }
    }

    /**
     * 获取饮食记录详情
     *
     * @param recordId 记录ID
     * @return 饮食记录详情
     */
    @GetMapping("/diet-record/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DietRecordResponseDTO>> getDietRecordDetail(@PathVariable Long recordId) {
        try {
            DietRecordResponseDTO record = dietRecordService.getAdminDietRecordDetail(recordId);

            if (record == null) {
                return ResponseEntity.ok(ApiResponse.error(404, "记录不存在"));
            }

            return ResponseEntity.ok(ApiResponse.success(record));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "获取饮食记录详情失败"));
        }
    }

    /**
     * 获取热门食物统计数据
     * 用于管理员仪表盘展示最受欢迎的食物
     *
     * @param period 时间周期：week(周)、month(月)、quarter(季度)，默认为month
     * @return 热门食物统计数据
     */
    @GetMapping("/popular-foods")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularFoods(
            @RequestParam(required = false, defaultValue = "month") String period) {

        try {
            // 调用服务层获取热门食物数据
            List<Map<String, Object>> popularFoods = dietRecordService.getPopularFoodsByPeriod(period, 10);
            return ResponseEntity.ok(ApiResponse.success(popularFoods));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "获取热门食物统计失败"));
        }
    }
}
