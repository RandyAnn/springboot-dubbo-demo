package com.example.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户营养目标实体类
 */
@Data
@TableName("user_nutrition_goals")
public class UserNutritionGoal implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 每日热量目标 (千卡)
     */
    private Integer calorieTarget;
    
    /**
     * 目标体重 (kg)
     */
    private BigDecimal weightTarget;
    
    /**
     * 蛋白质目标 (g)
     */
    private Integer proteinTarget;
    
    /**
     * 碳水化合物目标 (g)
     */
    private Integer carbsTarget;
    
    /**
     * 脂肪目标 (g)
     */
    private Integer fatTarget;
    
    /**
     * 是否是素食主义者
     */
    private Boolean isVegetarian;
    
    /**
     * 是否低碳水饮食
     */
    private Boolean isLowCarb;
    
    /**
     * 是否高蛋白饮食
     */
    private Boolean isHighProtein;
    
    /**
     * 是否无麸质饮食
     */
    private Boolean isGlutenFree;
    
    /**
     * 是否低钠饮食
     */
    private Boolean isLowSodium;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
} 