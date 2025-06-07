package com.example.diet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录食物明细实体类
 */
@Data
@TableName("diet_record_foods")
public class DietRecordFood implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dietRecordId;
    private Long foodId;
    private String foodName;
    private BigDecimal amount;
    private String unit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
    private BigDecimal grams;
    private LocalDateTime createdAt;
}
