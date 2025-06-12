package com.example.dashboard.command;

import lombok.Data;

/**
 * 营养趋势查询命令
 */
@Data
public class NutritionTrendCommand {
    
    /**
     * 时间周期：week(周)、month(月)、year(年)
     */
    private String period;
    
    /**
     * 创建营养趋势查询命令
     */
    public static NutritionTrendCommand of(String period) {
        NutritionTrendCommand command = new NutritionTrendCommand();
        command.setPeriod(period);
        return command;
    }
    
    /**
     * 创建默认的营养趋势查询命令（月度）
     */
    public static NutritionTrendCommand ofMonth() {
        return of("month");
    }
}
