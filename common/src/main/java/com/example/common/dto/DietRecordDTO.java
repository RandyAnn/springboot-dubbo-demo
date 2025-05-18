package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 饮食记录DTO
 */
@Data
public class DietRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;
    private String time;
    private String mealType;
    private String remark;
    private BigDecimal totalCalorie;
    private List<DietRecordFoodDTO> foods;
}