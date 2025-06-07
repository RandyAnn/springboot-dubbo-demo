package com.example.nutrition.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 营养建议显示DTO
 */
@Data
public class NutritionAdviceDisplayDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 无参构造函数
     */
    public NutritionAdviceDisplayDTO() {
    }

    /**
     * 带参构造函数
     */
    public NutritionAdviceDisplayDTO(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

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
}
