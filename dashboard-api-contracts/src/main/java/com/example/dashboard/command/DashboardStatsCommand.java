package com.example.dashboard.command;

import lombok.Data;

import java.time.LocalDate;

/**
 * 仪表盘统计数据查询命令
 */
@Data
public class DashboardStatsCommand {
    
    /**
     * 查询日期，默认为当天
     */
    private LocalDate date;
    
    /**
     * 是否包含详细统计
     */
    private Boolean includeDetails;
    
    /**
     * 创建默认的统计查询命令（当天数据）
     */
    public static DashboardStatsCommand ofToday() {
        DashboardStatsCommand command = new DashboardStatsCommand();
        command.setDate(LocalDate.now());
        command.setIncludeDetails(false);
        return command;
    }
    
    /**
     * 创建指定日期的统计查询命令
     */
    public static DashboardStatsCommand of(LocalDate date) {
        DashboardStatsCommand command = new DashboardStatsCommand();
        command.setDate(date);
        command.setIncludeDetails(false);
        return command;
    }
}
