package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 营养建议数据
 */
@Data
public class NutritionAdviceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
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
     * 构造函数
     */
    public NutritionAdviceDTO() {}
    
    /**
     * 构造函数
     * @param type 类型
     * @param title 标题
     * @param description 描述
     */
    public NutritionAdviceDTO(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }
} 