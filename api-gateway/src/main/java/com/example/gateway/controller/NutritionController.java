package com.example.gateway.controller;

import com.example.common.command.nutrition.NutritionAdviceCommand;
import com.example.common.command.nutrition.NutritionStatCommand;
import com.example.common.command.nutrition.NutritionTrendCommand;
import com.example.common.dto.nutrition.NutritionAdviceDisplayDTO;
import com.example.common.dto.nutrition.NutritionDetailItemDTO;
import com.example.common.dto.nutrition.NutritionStatDTO;
import com.example.common.dto.nutrition.NutritionTrendDTO;
import com.example.common.dto.nutrition.NutritionAdviceRequestDTO;
import com.example.common.dto.nutrition.NutritionStatRequestDTO;
import com.example.common.dto.nutrition.NutritionTrendRequestDTO;
import com.example.common.response.ApiResponse;
import com.example.common.service.NutritionStatService;
import com.example.common.util.SecurityContextUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 营养分析控制器
 */
@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    @DubboReference
    private NutritionStatService nutritionStatService;

    /**
     * 获取每日营养摄入统计
     * @param requestDTO 营养统计请求DTO
     * @return 营养统计数据
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<NutritionStatDTO>> getDailyNutrition(NutritionStatRequestDTO requestDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建命令对象
        NutritionStatCommand command = NutritionStatCommand.of(userId, requestDTO.getDateAsLocalDate());

        // 获取营养统计数据
        NutritionStatDTO nutritionStat = nutritionStatService.getDailyNutritionStat(command);

        return ResponseEntity.ok(ApiResponse.success(nutritionStat));
    }

    /**
     * 获取营养摄入趋势
     * @param requestDTO 营养趋势请求DTO
     * @return 营养趋势数据
     */
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<NutritionTrendDTO>> getNutritionTrend(NutritionTrendRequestDTO requestDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 获取日期参数
        LocalDate startDate = requestDTO.getStartDateAsLocalDate();
        LocalDate endDate = requestDTO.getEndDateAsLocalDate();

        // 限制最大日期范围为90天
        if (ChronoUnit.DAYS.between(startDate, endDate) > 90) {
            endDate = startDate.plus(90, ChronoUnit.DAYS);
        }

        // 创建命令对象
        NutritionTrendCommand command = NutritionTrendCommand.of(userId, startDate, endDate);

        // 获取营养趋势数据
        NutritionTrendDTO trendData = nutritionStatService.getNutritionTrend(command);

        return ResponseEntity.ok(ApiResponse.success(trendData));
    }

    /**
     * 获取营养摄入详情
     * @param requestDTO 营养统计请求DTO
     * @return 营养摄入详情列表
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<NutritionDetailItemDTO>>> getNutritionDetails(NutritionStatRequestDTO requestDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建命令对象
        NutritionStatCommand command = NutritionStatCommand.of(userId, requestDTO.getDateAsLocalDate());

        // 获取营养详情数据
        List<NutritionDetailItemDTO> detailList = nutritionStatService.getNutritionDetails(command);

        return ResponseEntity.ok(ApiResponse.success(detailList));
    }

    /**
     * 获取营养建议
     * @param requestDTO 营养建议请求DTO
     * @return 营养建议列表
     */
    @GetMapping("/advice")
    public ResponseEntity<ApiResponse<List<NutritionAdviceDisplayDTO>>> getNutritionAdvice(NutritionAdviceRequestDTO requestDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建命令对象
        NutritionAdviceCommand command = NutritionAdviceCommand.of(userId, requestDTO.getDateAsLocalDate());

        // 获取营养建议
        List<NutritionAdviceDisplayDTO> adviceList = nutritionStatService.getNutritionAdvice(command);

        return ResponseEntity.ok(ApiResponse.success(adviceList));
    }
}