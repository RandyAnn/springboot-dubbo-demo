package com.example.dashboard.service;

import com.example.dashboard.command.DashboardStatsCommand;
import com.example.dashboard.command.NutritionTrendCommand;
import com.example.dashboard.command.PopularFoodsCommand;
import com.example.dashboard.dto.DashboardStatsDTO;
import com.example.dashboard.dto.NutritionTrendDTO;
import com.example.dashboard.dto.PopularFoodDTO;
import com.example.diet.dto.DietRecordQueryDTO;
import com.example.diet.dto.DietRecordResponseDTO;
import com.example.shared.response.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取管理员仪表盘统计数据
     * 包括：总用户数、今日饮食记录数、营养达标率、推荐准确率等
     * 
     * @param command 统计数据查询命令
     * @return 统计数据DTO
     */
    DashboardStatsDTO getDashboardStats(DashboardStatsCommand command);

    /**
     * 获取用户营养摄入趋势数据
     * 用于管理员仪表盘展示所有用户的平均营养摄入趋势
     *
     * @param command 营养趋势查询命令
     * @return 营养摄入趋势数据
     */
    NutritionTrendDTO getNutritionTrend(NutritionTrendCommand command);

    /**
     * 获取最新饮食记录列表
     * 用于管理员仪表盘展示所有用户的最新饮食记录
     *
     * @param queryDTO 查询参数DTO
     * @return 饮食记录分页列表
     */
    PageResult<DietRecordResponseDTO> getLatestDietRecords(DietRecordQueryDTO queryDTO);

    /**
     * 获取饮食记录详情
     *
     * @param recordId 记录ID
     * @return 饮食记录详情
     */
    DietRecordResponseDTO getDietRecordDetail(Long recordId);

    /**
     * 获取热门食物统计
     *
     * @param command 命令对象
     * @return 热门食物列表
     */
    List<Map<String, Object>> getPopularFoods(PopularFoodsCommand command);
}
