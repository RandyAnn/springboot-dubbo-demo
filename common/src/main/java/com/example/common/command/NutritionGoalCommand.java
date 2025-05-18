package com.example.common.command;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 营养目标命令对象
 */
@Data
public class NutritionGoalCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 每日热量目标 (千卡)
     */
    private Integer calorieTarget;
    
    /**
     * 目标体重 (kg)
     */
    private BigDecimal weightTarget;
    
    /**
     * 蛋白质目标 (g)
     */
    private Integer proteinTarget;
    
    /**
     * 碳水化合物目标 (g)
     */
    private Integer carbsTarget;
    
    /**
     * 脂肪目标 (g)
     */
    private Integer fatTarget;
    
    /**
     * 是否是素食主义者
     */
    private Boolean isVegetarian;
    
    /**
     * 是否低碳水饮食
     */
    private Boolean isLowCarb;
    
    /**
     * 是否高蛋白饮食
     */
    private Boolean isHighProtein;
    
    /**
     * 是否无麸质饮食
     */
    private Boolean isGlutenFree;
    
    /**
     * 是否低钠饮食
     */
    private Boolean isLowSodium;
    
    /**
     * 创建一个新的命令对象，设置用户ID
     * 
     * @param userId 用户ID
     * @return 新的命令对象
     */
    public static NutritionGoalCommand withUserId(Long userId) {
        NutritionGoalCommand command = new NutritionGoalCommand();
        command.setUserId(userId);
        return command;
    }
}
