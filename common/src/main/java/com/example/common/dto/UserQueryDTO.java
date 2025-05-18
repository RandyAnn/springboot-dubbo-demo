package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 用户状态
     */
    private Integer status;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 时间过滤
     */
    private String timeFilter;
}
