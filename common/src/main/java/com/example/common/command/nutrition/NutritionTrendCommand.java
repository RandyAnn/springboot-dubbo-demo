package com.example.common.command.nutrition;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 营养趋势命令对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTrendCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 创建一个新的命令对象，设置用户ID、开始日期和结束日期
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 新的命令对象
     */
    public static NutritionTrendCommand of(Long userId, LocalDate startDate, LocalDate endDate) {
        return NutritionTrendCommand.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
