package com.example.common.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物分类更新命令对象
 */
@Data
public class FoodCategoryUpdateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Integer id;
    
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

    /**
     * 创建一个新的命令对象，设置分类ID
     *
     * @param categoryId 分类ID
     * @return 新的命令对象
     */
    public static FoodCategoryUpdateCommand withCategoryId(Integer categoryId) {
        FoodCategoryUpdateCommand command = new FoodCategoryUpdateCommand();
        command.setId(categoryId);
        return command;
    }
}
