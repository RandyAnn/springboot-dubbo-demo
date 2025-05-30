package com.example.common.command.diet;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食记录查询命令对象
 */
@Data
public class DietRecordQueryCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 开始日期
     */
    private String startDate;
    
    /**
     * 结束日期
     */
    private String endDate;
    
    /**
     * 餐次类型
     */
    private String mealType;
    
    /**
     * 当前页，默认第1页
     */
    private Integer page = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer size = 10;
    
    /**
     * 创建一个新的命令对象，设置用户ID
     *
     * @param userId 用户ID
     * @return 新的命令对象
     */
    public static DietRecordQueryCommand withUserId(Long userId) {
        DietRecordQueryCommand command = new DietRecordQueryCommand();
        command.setUserId(userId);
        return command;
    }
}
