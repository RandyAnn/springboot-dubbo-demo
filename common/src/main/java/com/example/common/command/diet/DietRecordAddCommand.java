package com.example.common.command.diet;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 饮食记录添加命令对象
 */
@Data
public class DietRecordAddCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
    
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
    private List<DietRecordFoodCommand> foods;
    
    /**
     * 创建一个新的命令对象，设置用户ID
     *
     * @param userId 用户ID
     * @return 新的命令对象
     */
    public static DietRecordAddCommand withUserId(Long userId) {
        DietRecordAddCommand command = new DietRecordAddCommand();
        command.setUserId(userId);
        return command;
    }
    
    /**
     * 饮食记录食物命令对象
     */
    @Data
    public static class DietRecordFoodCommand implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long foodId;
        private String name;
        private BigDecimal amount;
        private String unit;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal carbs;
        private BigDecimal grams;
    }
}
