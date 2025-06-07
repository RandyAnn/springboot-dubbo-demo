package com.example.nutrition.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 营养建议管理请求DTO
 */
@Data
public class NutritionAdviceManageRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 建议类型: warning, info, danger, success
     */
    @NotBlank(message = "建议类型不能为空")
    @Pattern(regexp = "^(warning|info|danger|success)$", message = "建议类型只能是warning、info、danger或success")
    private String type;
    
    /**
     * 建议标题
     */
    @NotBlank(message = "建议标题不能为空")
    private String title;
    
    /**
     * 建议详情
     */
    @NotBlank(message = "建议详情不能为空")
    private String description;
    
    /**
     * 条件类型: protein, carbs, fat, calorie, default
     */
    @NotBlank(message = "条件类型不能为空")
    @Pattern(regexp = "^(protein|carbs|fat|calorie|default)$", message = "条件类型只能是protein、carbs、fat、calorie或default")
    private String conditionType;
    
    /**
     * 最小百分比阈值
     */
    @Min(value = 0, message = "最小百分比不能小于0")
    @Max(value = 999, message = "最小百分比不能大于999")
    private Integer minPercentage;
    
    /**
     * 最大百分比阈值
     */
    @Min(value = 0, message = "最大百分比不能小于0")
    @Max(value = 999, message = "最大百分比不能大于999")
    private Integer maxPercentage;
    
    /**
     * 是否为默认建议
     */
    private Boolean isDefault = false;
    
    /**
     * 优先级，数字越大优先级越高
     */
    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 100, message = "优先级不能大于100")
    private Integer priority = 10;
    
    /**
     * 状态：1-启用，0-禁用
     */
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status = 1;
}
