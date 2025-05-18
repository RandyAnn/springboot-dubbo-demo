package com.example.common.service;

import com.example.common.dto.HealthReportDTO;

import java.time.LocalDate;

/**
 * 健康报告服务接口
 */
public interface HealthReportService {
    
    /**
     * 获取用户健康报告
     * @param userId 用户ID
     * @param date 日期
     * @return 健康报告数据
     */
    HealthReportDTO getHealthReport(Long userId, LocalDate date);
} 