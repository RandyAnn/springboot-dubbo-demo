package com.example.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 周进度对比DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyProgressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 热量对比
     */
    private NutrientProgressDTO calorie;

    /**
     * 蛋白质对比
     */
    private NutrientProgressDTO protein;

    /**
     * 碳水化合物对比
     */
    private NutrientProgressDTO carbs;

    /**
     * 脂肪对比
     */
    private NutrientProgressDTO fat;
}
