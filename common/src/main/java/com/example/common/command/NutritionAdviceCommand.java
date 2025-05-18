package com.example.common.command;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 营养建议命令对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAdviceCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 创建一个新的命令对象，设置用户ID和日期
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 新的命令对象
     */
    public static NutritionAdviceCommand of(Long userId, LocalDate date) {
        return NutritionAdviceCommand.builder()
                .userId(userId)
                .date(date)
                .build();
    }
}
