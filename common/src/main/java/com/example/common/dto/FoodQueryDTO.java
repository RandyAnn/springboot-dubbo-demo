package com.example.common.dto;

import java.io.Serializable;

/**
 * 食物查询参数DTO
 */
public class FoodQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyword;   // 搜索关键词
    private Integer categoryId; // 分类ID
    private Integer current = 1;  // 当前页，默认第1页
    private Integer size = 10;    // 每页大小，默认10条

    // getter and setter
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }



    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}