package com.example.nutrition.controller;

import com.example.nutrition.command.NutritionAdviceCommand;
import com.example.nutrition.command.NutritionStatCommand;
import com.example.nutrition.command.NutritionTrendCommand;
import com.example.nutrition.dto.NutritionAdviceDisplayDTO;
import com.example.nutrition.dto.NutritionDetailItemDTO;
import com.example.nutrition.dto.NutritionStatDTO;
import com.example.nutrition.dto.NutritionTrendDTO;
import com.example.nutrition.dto.NutritionAdviceRequestDTO;
import com.example.nutrition.dto.NutritionStatRequestDTO;
import com.example.nutrition.dto.NutritionTrendRequestDTO;
import com.example.shared.response.ApiResponse;
import com.example.nutrition.service.NutritionStatService;
import com.example.shared.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
        NutritionTrendDTO nutritionTrend = nutritionStatService.getNutritionTrend(command);

        return ResponseEntity.ok(ApiResponse.success(nutritionTrend));
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
