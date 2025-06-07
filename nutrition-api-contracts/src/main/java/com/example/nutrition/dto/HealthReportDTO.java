package com.example.nutrition.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 健康报告DTO
 */
@Data
public class HealthReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 营养统计
     */
    private NutritionStatDTO nutritionStat;

    /**
     * 营养建议列表
     */
    private List<NutritionAdviceDisplayDTO> advices;

    /**
     * 周进度
     */
    private WeeklyProgressDTO weeklyProgress;

    /**
     * 建议
     */
    private String suggestion;

    /**
     * 健康分数
     */
    private Integer healthScore;

    /**
     * 分数变化
     */
    private Integer scoreChange;

    /**
     * 营养平衡
     */
    private NutritionBalanceDTO nutritionBalance;
}
