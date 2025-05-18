package com.example.common.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物查询命令对象
 */
@Data
public class FoodQueryCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 当前页，默认第1页
     */
    private Integer current = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer size = 10;
}
