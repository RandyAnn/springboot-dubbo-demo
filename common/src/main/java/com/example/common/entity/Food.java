package com.example.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 食物实体类，对应数据库中的food表
 */
@Data
@TableName("food")
public class Food implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String foodName;
    private String measure;
    private Double grams;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double satFat;
    private String fiber;
    private Double carbs;
    private String imageUrl; // 食物图片URL
    private Integer categoryId; // 分类ID
}
