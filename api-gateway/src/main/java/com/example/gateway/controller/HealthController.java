package com.example.gateway.controller;

import com.example.common.dto.HealthReportDTO;
import com.example.common.response.ApiResponse;
import com.example.common.service.HealthReportService;
import com.example.common.util.SecurityContextUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 健康报告控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @DubboReference
    private HealthReportService healthReportService;

    /**
     * 获取健康报告
     * @param date 日期，格式：yyyy-MM-dd，默认为当天
     * @return 健康报告数据
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<HealthReportDTO>> getHealthReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        // 如果未提供日期，使用当天
        if (date == null) {
            date = LocalDate.now();
        }

        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 获取健康报告数据
        HealthReportDTO healthReport = healthReportService.getHealthReport(userId, date);

        return ResponseEntity.ok(ApiResponse.success(healthReport));
    }
}