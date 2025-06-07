package com.example.diet.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 饮食记录请求DTO
 */
@Data
public class DietRecordRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录日期
     */
    private String date;
    
    /**
     * 记录时间
     */
    private String time;
    
    /**
     * 餐次类型: breakfast/lunch/dinner/snacks
     */
    private String mealType;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 总热量(千卡)
     */
    private BigDecimal totalCalorie;
    
    /**
     * 食物列表
     */
    private List<DietRecordFoodDTO> foods;
}
