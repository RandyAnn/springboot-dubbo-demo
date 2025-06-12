package com.example.dashboard.command;

import lombok.Data;

/**
 * 热门食物查询命令
 */
@Data
public class PopularFoodsCommand {
    
    /**
     * 时间周期：week(周)、month(月)、quarter(季度)
     */
    private String period;
    
    /**
     * 返回数量限制
     */
    private Integer limit;
    
    /**
     * 创建热门食物查询命令
     */
    public static PopularFoodsCommand of(String period, Integer limit) {
        PopularFoodsCommand command = new PopularFoodsCommand();
        command.setPeriod(period);
        command.setLimit(limit);
        return command;
    }
    
    /**
     * 创建默认的热门食物查询命令（月度，前10名）
     */
    public static PopularFoodsCommand ofDefault() {
        return of("month", 10);
    }
}
