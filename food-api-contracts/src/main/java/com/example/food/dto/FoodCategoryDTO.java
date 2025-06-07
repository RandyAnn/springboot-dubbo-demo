package com.example.food.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 食物分类DTO，用于返回给前端
 */
@Data
public class FoodCategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;       // 分类ID
    private String name;      // 分类名称
    private String description; // 分类描述
    private String color;     // 分类颜色
    private Integer sortOrder; // 排序顺序
    private Integer foodCount; // 该分类下的食物数量（非数据库字段）
}
