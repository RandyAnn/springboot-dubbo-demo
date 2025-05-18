package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 饮食记录食物DTO
 */
@Data
public class DietRecordFoodDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long foodId;
    private String name;
    private BigDecimal amount;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private BigDecimal grams;
} 