package com.example.dashboard.controller;

import com.example.dashboard.command.DashboardStatsCommand;
import com.example.dashboard.command.NutritionTrendCommand;
import com.example.dashboard.command.PopularFoodsCommand;
import com.example.dashboard.dto.DashboardStatsDTO;
import com.example.dashboard.dto.NutritionTrendDTO;
import com.example.dashboard.dto.PopularFoodDTO;
import com.example.dashboard.service.DashboardService;
import com.example.diet.dto.DietRecordQueryDTO;
import com.example.diet.dto.DietRecordResponseDTO;
import com.example.shared.exception.BusinessException;
import com.example.shared.response.ApiResponse;
import com.example.shared.response.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取管理员仪表盘统计数据
     * 包括：总用户数、今日饮食记录数、营养达标率等
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsCommand command = DashboardStatsCommand.ofToday();
        DashboardStatsDTO stats = dashboardService.getDashboardStats(command);
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
    public ResponseEntity<ApiResponse<NutritionTrendDTO>> getNutritionTrend(
            @RequestParam(required = false, defaultValue = "month") String period) {

        NutritionTrendCommand command = NutritionTrendCommand.of(period);
        NutritionTrendDTO trendData = dashboardService.getNutritionTrend(command);
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
        PageResult<DietRecordResponseDTO> records = dashboardService.getLatestDietRecords(queryDTO);
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
        DietRecordResponseDTO record = dashboardService.getDietRecordDetail(recordId);

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

        PopularFoodsCommand command = new PopularFoodsCommand();
        command.setPeriod(period);
        command.setLimit(10); // 默认限制10条

        List<Map<String, Object>> popularFoods = dashboardService.getPopularFoods(command);
        return ResponseEntity.ok(ApiResponse.success(popularFoods));
    }
}
