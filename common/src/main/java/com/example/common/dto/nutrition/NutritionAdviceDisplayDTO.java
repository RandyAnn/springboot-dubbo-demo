package com.example.common.dto.nutrition;

import lombok.Data;
import java.io.Serializable;

/**
 * 营养建议显示DTO（简化版，用于前端显示）
 */
@Data
public class NutritionAdviceDisplayDTO implements Serializable {
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
    public NutritionAdviceDisplayDTO() {}

    /**
     * 构造函数
     * @param type 类型
     * @param title 标题
     * @param description 描述
     */
    public NutritionAdviceDisplayDTO(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }
}