package com.example.gateway.controller;

import com.example.common.command.DietRecordAddCommand;
import com.example.common.command.DietRecordDeleteCommand;
import com.example.common.command.DietRecordQueryCommand;
import com.example.common.dto.DietRecordDTO;
import com.example.common.dto.DietRecordQueryDTO;
import com.example.common.dto.DietRecordRequestDTO;
import com.example.common.dto.DietRecordResponseDTO;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.DietRecordService;
import com.example.common.util.SecurityContextUtil;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 饮食记录控制器
 */
@RestController
@RequestMapping("/api/diet-records")
public class DietRecordController {

    @DubboReference
    private DietRecordService dietRecordService;

    /**
     * 添加饮食记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> addDietRecord(@RequestBody DietRecordRequestDTO requestDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建Command对象
        DietRecordAddCommand command = DietRecordAddCommand.withUserId(userId);
        BeanUtils.copyProperties(requestDTO, command);

        // 转换食物列表
        if (requestDTO.getFoods() != null && !requestDTO.getFoods().isEmpty()) {
            command.setFoods(requestDTO.getFoods().stream()
                .map(foodDTO -> {
                    DietRecordAddCommand.DietRecordFoodCommand foodCommand = new DietRecordAddCommand.DietRecordFoodCommand();
                    BeanUtils.copyProperties(foodDTO, foodCommand);
                    return foodCommand;
                })
                .collect(java.util.stream.Collectors.toList()));
        }

        Long recordId = dietRecordService.addDietRecord(command);

        Map<String, Object> data = new HashMap<>();
        data.put("recordId", recordId);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取饮食记录列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<DietRecordResponseDTO>>> getDietRecords(DietRecordQueryDTO queryDTO) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建Command对象
        DietRecordQueryCommand command = DietRecordQueryCommand.withUserId(userId);
        // 复制属性时忽略userId字段，防止覆盖
        BeanUtils.copyProperties(queryDTO, command, "userId");

        PageResult<DietRecordResponseDTO> pageResult = dietRecordService.getDietRecords(command);

        return ResponseEntity.ok(ApiResponse.success(pageResult));
    }

    /**
     * 获取饮食记录详情
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<DietRecordResponseDTO>> getDietRecordDetail(@PathVariable Long recordId) {
        DietRecordResponseDTO record = dietRecordService.getDietRecordDetail(recordId);

        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }

        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteDietRecord(@PathVariable Long recordId) {
        // 从认证对象中获取userId
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建Command对象
        DietRecordDeleteCommand command = DietRecordDeleteCommand.of(userId, recordId);
        boolean success = dietRecordService.deleteDietRecord(command);

        if (!success) {
            throw new BusinessException(400, "删除失败");
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}