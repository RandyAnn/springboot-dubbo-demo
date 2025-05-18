package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 健康报告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 健康评分（0-100）
     */
    private Integer healthScore;
    
    /**
     * 评分变化（相比上周）
     */
    private Integer scoreChange;
    
    /**
     * 营养平衡数据
     */
    private NutritionBalanceDTO nutritionBalance;
    
    /**
     * 周进度对比数据
     */
    private WeeklyProgressDTO weeklyProgress;
} 