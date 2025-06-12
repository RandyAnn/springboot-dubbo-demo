package com.example.dashboard.dto;

import lombok.Data;

import java.util.List;

/**
 * 营养趋势数据DTO
 */
@Data
public class NutritionTrendDTO {
    
    /**
     * 日期列表
     */
    private List<String> dateList;
    
    /**
     * 热量列表
     */
    private List<Double> calorieList;
    
    /**
     * 蛋白质列表
     */
    private List<Double> proteinList;
    
    /**
     * 碳水化合物列表
     */
    private List<Double> carbsList;
    
    /**
     * 脂肪列表
     */
    private List<Double> fatList;
    
    /**
     * 时间周期
     */
    private String period;
    
    /**
     * 数据点数量
     */
    private Integer dataPoints;
}
