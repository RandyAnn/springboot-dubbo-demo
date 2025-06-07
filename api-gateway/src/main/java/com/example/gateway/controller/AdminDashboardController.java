package com.example.gateway.controller;

import com.example.diet.command.DietRecordQueryCommand;
import com.example.diet.dto.DietRecordQueryDTO;
import com.example.diet.dto.DietRecordResponseDTO;
import com.example.shared.exception.BusinessException;
import com.example.shared.response.ApiResponse;
import com.example.shared.response.PageResult;
import com.example.diet.service.DietRecordService;
import com.example.nutrition.service.NutritionStatService;
import com.example.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘控制器
 * 提供管理员仪表盘相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @DubboReference
    private UserService userService;

    @DubboReference
    private DietRecordService dietRecordService;

    @DubboReference
    private NutritionStatService nutritionStatService;

    /**
     * 获取管理员仪表盘统计数据
     * 包括：总用户数、今日饮食记录数、营养达标率等
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取总用户数（直接查询，性能更好）
        long totalUsers = userService.getTotalUserCount();
        stats.put("totalUsers", totalUsers);

        // 获取今日饮食记录数
        LocalDate today = LocalDate.now();
        int todayRecords = dietRecordService.countDietRecordsByDate(today);
        stats.put("todayRecords", todayRecords);

        // 计算营养达标率
        double nutritionComplianceRate = nutritionStatService.calculateNutritionComplianceRate(today);
        stats.put("nutritionComplianceRate", nutritionComplianceRate);

        // 推荐准确率（模拟数据）
        stats.put("recommendationAccuracy", 95);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取用户营养摄入趋势数据
     * 用于管理员仪表盘展示所有用户的平均营养摄入趋势
     *
     * @param period 时间周期：week(周)、month(月)、year(年)，默认为month
     * @return 营养摄入趋势数据
     */
    @GetMapping("/nutrition-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNutritionTrend(
            @RequestParam(required = false, defaultValue = "month") String period) {

        Map<String, Object> trendData = nutritionStatService.getAllNutritionTrend(period);
        return ResponseEntity.ok(ApiResponse.success(trendData));
    }

    /**
     * 获取最新饮食记录列表
     * 用于管理员仪表盘展示所有用户的最新饮食记录
     *
     * @param queryDTO 查询参数DTO
     * @return 饮食记录列表
     */
    @GetMapping("/latest-diet-records")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<DietRecordResponseDTO>>> getLatestDietRecords(DietRecordQueryDTO queryDTO) {
        // 构建查询命令对象
        DietRecordQueryCommand command = new DietRecordQueryCommand();
        BeanUtils.copyProperties(queryDTO, command);

        // 直接调用getAllUsersDietRecords，它会处理userId的逻辑
        PageResult<DietRecordResponseDTO> records = dietRecordService.getAllUsersDietRecords(command);

        return ResponseEntity.ok(ApiResponse.success(records));
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
        DietRecordResponseDTO record = dietRecordService.getDietRecordDetail(recordId);

        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }

        return ResponseEntity.ok(ApiResponse.success(record));
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

        // 调用服务层获取热门食物数据
        List<Map<String, Object>> popularFoods = dietRecordService.getPopularFoodsByPeriod(period, 10);
        return ResponseEntity.ok(ApiResponse.success(popularFoods));
    }
}
