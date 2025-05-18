package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 健康成就DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthAchievementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 成就ID
     */
    private Long id;
    
    /**
     * 成就名称
     */
    private String name;
    
    /**
     * 成就描述
     */
    private String description;
    
    /**
     * 成就图标路径
     */
    private String icon;
    
    /**
     * 进度百分比（0-100）
     */
    private Integer progress;
    
    /**
     * 当前进度值
     */
    private Integer current;
    
    /**
     * 目标值
     */
    private Integer target;
    
    /**
     * 进度单位
     */
    private String unit;
} 