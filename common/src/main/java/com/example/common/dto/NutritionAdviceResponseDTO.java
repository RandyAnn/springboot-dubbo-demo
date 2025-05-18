package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 营养建议响应DTO
 */
@Data
public class NutritionAdviceResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 建议ID
     */
    private Long id;
    
    /**
     * 建议类型: warning, info, danger, success
     */
    private String type;
    
    /**
     * 建议标题
     */
    private String title;
    
    /**
     * 建议详情
     */
    private String description;
    
    /**
     * 条件类型: protein, carbs, fat, calorie, default
     */
    private String conditionType;
    
    /**
     * 最小百分比阈值
     */
    private Integer minPercentage;
    
    /**
     * 最大百分比阈值
     */
    private Integer maxPercentage;
    
    /**
     * 是否为默认建议
     */
    private Boolean isDefault;
    
    /**
     * 优先级，数字越大优先级越高
     */
    private Integer priority;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
