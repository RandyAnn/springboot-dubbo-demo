package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 饮食记录响应DTO
 */
@Data
public class DietRecordResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private LocalDate date;
    private LocalTime time;
    private String mealType;
    private String remark;
    private BigDecimal totalCalorie;
    private List<DietRecordFoodDTO> foods;
}