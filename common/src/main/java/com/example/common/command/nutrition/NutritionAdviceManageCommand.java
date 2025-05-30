package com.example.common.command.nutrition;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 营养建议管理命令对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAdviceManageCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 建议ID（更新时使用）
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
}
