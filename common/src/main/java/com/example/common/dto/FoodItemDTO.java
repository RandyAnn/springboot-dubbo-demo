package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 食物条目DTO，用于返回给前端
 */
@Data
public class FoodItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;       // 食物ID
    private String name;      // 食物名称
    private String measure;   // 份量描述
    private Double grams;     // 克数
    private Double calories;  // 卡路里
    private Double protein;   // 蛋白质(g)
    private Double fat;       // 脂肪(g)
    private Double satFat;    // 饱和脂肪(g)
    private Double carbs;     // 碳水(g)
    private String category;  // 分类
    private Integer categoryId; // 分类ID
    private String desc;      // 描述信息
    private String unit;      // 单位
    private String imageUrl; //图片
    private String remark; //备注
}