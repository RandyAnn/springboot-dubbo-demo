package com.example.nutrition.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 营养素摄入详情项
 */
@Data
public class NutritionDetailItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 营养素名称
     */
    private String name;

    /**
     * 营养素值
     */
    private Double value;

    /**
     * 营养素单位
     */
    private String unit;

    /**
     * 完成百分比
     */
    private Double percentage;

    /**
     * 构造函数
     */
    public NutritionDetailItemDTO() {}

    /**
     * 构造函数
     * @param name 名称
     * @param value 值
     * @param unit 单位
     * @param percentage 百分比
     */
    public NutritionDetailItemDTO(String name, Double value, String unit, Double percentage) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.percentage = percentage;
    }
}
