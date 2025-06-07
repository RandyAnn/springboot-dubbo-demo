package com.example.nutrition.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户每日营养统计数据
 */
@Data
public class NutritionStatDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 日期，格式：yyyy-MM-dd
     */
    private String date;
    
    /**
     * 卡路里/热量（千卡）
     */
    private Integer calorie;
    
    /**
     * 蛋白质（克）
     */
    private Double protein;
    
    /**
     * 碳水化合物（克）
     */
    private Double carbs;
    
    /**
     * 脂肪（克）
     */
    private Double fat;
    
    /**
     * 热量目标达成百分比
     */
    private Double caloriePercentage;
    
    /**
     * 蛋白质目标达成百分比
     */
    private Double proteinPercentage;
    
    /**
     * 碳水目标达成百分比
     */
    private Double carbsPercentage;
    
    /**
     * 脂肪目标达成百分比
     */
    private Double fatPercentage;
}
