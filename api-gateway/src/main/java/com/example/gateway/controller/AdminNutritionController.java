package com.example.gateway.controller;

import com.example.nutrition.command.NutritionAdviceManageCommand;
import com.example.nutrition.dto.NutritionAdviceManageRequestDTO;
import com.example.nutrition.dto.NutritionAdviceResponseDTO;
import com.example.shared.exception.BusinessException;
import com.example.shared.response.ApiResponse;
import com.example.nutrition.service.NutritionAdviceService;
import com.example.nutrition.service.NutritionStatService;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 后台管理-营养分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/nutrition")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNutritionController {

    @DubboReference
    private NutritionStatService nutritionStatService;

    @DubboReference
    private NutritionAdviceService nutritionAdviceService;

    /**
     * 获取营养摄入趋势数据
     * @param period 时间周期：week(周)、month(月)、year(年)
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNutritionTrend(
            @RequestParam(defaultValue = "month") String period) {
        try {
            Map<String, Object> trendData = nutritionStatService.getAllNutritionTrend(period);
            return ResponseEntity.ok(ApiResponse.success(trendData));
        } catch (Exception e) {
            log.error("获取营养摄入趋势数据失败，时间周期: {}", period, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取营养摄入趋势数据失败"));
        }
    }

    /**
     * 获取营养达标率
     * @param date 日期，默认为当天
     * @return 营养达标率
     */
    @GetMapping("/compliance-rate")
    public ResponseEntity<ApiResponse<Double>> getNutritionComplianceRate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        double complianceRate = nutritionStatService.calculateNutritionComplianceRate(date);
        return ResponseEntity.ok(ApiResponse.success(complianceRate));
    }

    /**
     * 获取所有营养建议
     * @return 营养建议列表
     */
    @GetMapping("/advice")
    public ResponseEntity<ApiResponse<List<NutritionAdviceResponseDTO>>> getAllAdvices() {
        List<NutritionAdviceResponseDTO> adviceList = nutritionAdviceService.getAllAdvices();
        return ResponseEntity.ok(ApiResponse.success(adviceList));
    }

    /**
     * 根据ID获取营养建议
     * @param id 营养建议ID
     * @return 营养建议
     */
    @GetMapping("/advice/{id}")
    public ResponseEntity<ApiResponse<NutritionAdviceResponseDTO>> getAdviceById(@PathVariable Long id) {
        NutritionAdviceResponseDTO advice = nutritionAdviceService.getAdviceById(id);
        if (advice == null) {
            throw new BusinessException(404, "营养建议不存在");
        }
        return ResponseEntity.ok(ApiResponse.success(advice));
    }

    /**
     * 创建营养建议
     * @param requestDTO 营养建议管理请求DTO
     * @return 创建后的营养建议
     */
    @PostMapping("/advice")
    public ResponseEntity<ApiResponse<NutritionAdviceResponseDTO>> createAdvice(
            @RequestBody @Valid NutritionAdviceManageRequestDTO requestDTO) {
        // 创建命令对象
        NutritionAdviceManageCommand command = new NutritionAdviceManageCommand();
        command.setType(requestDTO.getType());
        command.setTitle(requestDTO.getTitle());
        command.setDescription(requestDTO.getDescription());
        command.setConditionType(requestDTO.getConditionType());
        command.setMinPercentage(requestDTO.getMinPercentage());
        command.setMaxPercentage(requestDTO.getMaxPercentage());
        command.setIsDefault(requestDTO.getIsDefault());
        command.setPriority(requestDTO.getPriority());
        command.setStatus(requestDTO.getStatus());

        // 调用服务创建营养建议
        NutritionAdviceResponseDTO createdAdvice = nutritionAdviceService.createAdvice(command);

        return ResponseEntity.ok(ApiResponse.success(createdAdvice));
    }

    /**
     * 更新营养建议
     * @param id 营养建议ID
     * @param requestDTO 营养建议管理请求DTO
     * @return 更新后的营养建议
     */
    @PutMapping("/advice/{id}")
    public ResponseEntity<ApiResponse<NutritionAdviceResponseDTO>> updateAdvice(
            @PathVariable Long id,
            @RequestBody @Valid NutritionAdviceManageRequestDTO requestDTO) {
        // 检查营养建议是否存在
        NutritionAdviceResponseDTO existingAdvice = nutritionAdviceService.getAdviceById(id);
        if (existingAdvice == null) {
            throw new BusinessException(404, "营养建议不存在");
        }

        // 创建命令对象
        NutritionAdviceManageCommand command = new NutritionAdviceManageCommand();
        command.setId(id); // 设置ID到command中
        command.setType(requestDTO.getType());
        command.setTitle(requestDTO.getTitle());
        command.setDescription(requestDTO.getDescription());
        command.setConditionType(requestDTO.getConditionType());
        command.setMinPercentage(requestDTO.getMinPercentage());
        command.setMaxPercentage(requestDTO.getMaxPercentage());
        command.setIsDefault(requestDTO.getIsDefault());
        command.setPriority(requestDTO.getPriority());
        command.setStatus(requestDTO.getStatus());

        // 调用服务更新营养建议
        NutritionAdviceResponseDTO updatedAdvice = nutritionAdviceService.updateAdvice(command);

        return ResponseEntity.ok(ApiResponse.success(updatedAdvice));
    }

    /**
     * 删除营养建议
     * @param id 营养建议ID
     * @return 操作结果
     */
    @DeleteMapping("/advice/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteAdvice(@PathVariable Long id) {
        boolean result = nutritionAdviceService.deleteAdvice(id);
        if (!result) {
            throw new BusinessException(404, "营养建议不存在");
        }
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    /**
     * 根据条件类型获取营养建议
     * @param conditionType 条件类型
     * @return 营养建议列表
     */
    @GetMapping("/advice/condition/{conditionType}")
    public ResponseEntity<ApiResponse<List<NutritionAdviceResponseDTO>>> getAdvicesByConditionType(
            @PathVariable String conditionType) {
        List<NutritionAdviceResponseDTO> adviceList = nutritionAdviceService.getAdvicesByConditionType(conditionType);
        return ResponseEntity.ok(ApiResponse.success(adviceList));
    }
}
