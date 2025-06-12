package com.example.nutrition.controller;

import com.example.nutrition.dto.HealthReportDTO;
import com.example.shared.response.ApiResponse;
import com.example.nutrition.service.HealthReportService;
import com.example.shared.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
