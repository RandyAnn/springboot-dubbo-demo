package com.example.dashboard.dto;

import lombok.Data;

/**
 * 热门食物DTO
 */
@Data
public class PopularFoodDTO {
    
    /**
     * 食物ID
     */
    private Long foodId;
    
    /**
     * 食物名称
     */
    private String foodName;
    
    /**
     * 食物图片URL
     */
    private String imageUrl;
    
    /**
     * 使用次数
     */
    private Long usageCount;
    
    /**
     * 使用用户数
     */
    private Long userCount;
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 热量（每100g）
     */
    private Integer calorie;
    
    /**
     * 分类名称
     */
    private String categoryName;
}
