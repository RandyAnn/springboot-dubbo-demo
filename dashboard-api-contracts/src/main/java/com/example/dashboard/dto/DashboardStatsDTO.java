package com.example.dashboard.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 仪表盘统计数据DTO
 */
@Data
public class DashboardStatsDTO {
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 今日饮食记录数
     */
    private Integer todayRecords;
    
    /**
     * 营养达标率（百分比）
     */
    private Double nutritionComplianceRate;
    
    /**
     * 推荐准确率（百分比）
     */
    private Integer recommendationAccuracy;
    
    /**
     * 统计日期
     */
    private LocalDate statisticsDate;
    
    /**
     * 活跃用户数（可选）
     */
    private Long activeUsers;
    
    /**
     * 本周新增用户数（可选）
     */
    private Long weeklyNewUsers;
    
    /**
     * 本月新增用户数（可选）
     */
    private Long monthlyNewUsers;
}
