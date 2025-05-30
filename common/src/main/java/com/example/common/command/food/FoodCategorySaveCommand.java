package com.example.common.command.food;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物分类保存命令对象
 */
@Data
public class FoodCategorySaveCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 分类颜色
     */
    private String color;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
