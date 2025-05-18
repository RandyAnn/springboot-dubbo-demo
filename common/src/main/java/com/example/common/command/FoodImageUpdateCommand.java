package com.example.common.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物图片更新命令对象
 */
@Data
public class FoodImageUpdateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer foodId;   // 食物ID
    private String imageUrl;  // 图片URL

    /**
     * 创建一个新的命令对象，设置食物ID
     *
     * @param foodId 食物ID
     * @return 新的命令对象
     */
    public static FoodImageUpdateCommand withFoodId(Integer foodId) {
        FoodImageUpdateCommand command = new FoodImageUpdateCommand();
        command.setFoodId(foodId);
        return command;
    }
}
