package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户营养摄入趋势数据
 */
@Data
public class NutritionTrendDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 日期列表，格式：yyyy-MM-dd
     */
    private List<String> dateList;
    
    /**
     * 卡路里/热量列表（千卡）
     */
    private List<Integer> calorieList;
    
    /**
     * 蛋白质列表（克）
     */
    private List<Double> proteinList;
    
    /**
     * 碳水化合物列表（克）
     */
    private List<Double> carbsList;
    
    /**
     * 脂肪列表（克）
     */
    private List<Double> fatList;
} 