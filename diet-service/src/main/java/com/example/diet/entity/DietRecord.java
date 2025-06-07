package com.example.diet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 饮食记录实体类
 */
@Data
@TableName("diet_records")
public class DietRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime time;
    private String mealType;
    private String remark;
    private BigDecimal totalCalorie;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
