package com.example.common.command.food;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物更新命令对象
 */
@Data
public class FoodUpdateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;       // 食物ID
    private String name;      // 食物名称
    private String measure;   // 份量描述
    private Double grams;     // 克数
    private Double calories;  // 卡路里
    private Double protein;   // 蛋白质(g)
    private Double fat;       // 脂肪(g)
    private Double satFat;    // 饱和脂肪(g)
    private Double carbs;     // 碳水(g)
    private Integer categoryId; // 分类ID
    private String imageUrl;  // 图片URL

    /**
     * 创建一个新的命令对象，设置食物ID
     *
     * @param foodId 食物ID
     * @return 新的命令对象
     */
    public static FoodUpdateCommand withFoodId(Integer foodId) {
        FoodUpdateCommand command = new FoodUpdateCommand();
        command.setId(foodId);
        return command;
    }
}
