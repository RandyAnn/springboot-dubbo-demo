package com.example.common.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 营养平衡DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionBalanceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 蛋白质达成百分比
     */
    private Integer protein;
    
    /**
     * 碳水化合物达成百分比
     */
    private Integer carbs;
    
    /**
     * 脂肪达成百分比
     */
    private Integer fat;
} 