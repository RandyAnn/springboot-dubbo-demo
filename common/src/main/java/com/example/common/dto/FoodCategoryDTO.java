package com.example.common.dto;

import java.io.Serializable;

/**
 * 食物分类DTO，用于返回给前端
 */
public class FoodCategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;       // 分类ID
    private String name;      // 分类名称
    private String description; // 分类描述
    private String color;     // 分类颜色
    private Integer sortOrder; // 排序顺序
    private Integer foodCount; // 该分类下的食物数量（非数据库字段）
    
    // getter and setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Integer getFoodCount() {
        return foodCount;
    }
    
    public void setFoodCount(Integer foodCount) {
        this.foodCount = foodCount;
    }
}
