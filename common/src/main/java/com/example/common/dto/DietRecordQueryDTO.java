package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食记录查询DTO
 */
@Data
public class DietRecordQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String startDate;
    private String endDate;
    private String mealType;
    private Integer page = 1;
    private Integer size = 10;
    
    /**
     * 用户ID - 主要用于管理员后台查询特定用户的记录
     * 普通用户接口不需要设置此字段，接口内部会自动获取当前登录用户ID
     */
    private Long userId;
} 