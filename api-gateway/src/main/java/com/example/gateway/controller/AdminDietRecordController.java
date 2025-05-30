package com.example.gateway.controller;

import com.example.common.command.diet.DietRecordDeleteCommand;
import com.example.common.command.diet.DietRecordQueryCommand;
import com.example.common.dto.diet.DietRecordQueryDTO;
import com.example.common.dto.diet.DietRecordResponseDTO;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.DietRecordService;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 饮食记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/diet-records")
public class AdminDietRecordController {

    @DubboReference
    private DietRecordService dietRecordService;

    /**
     * 分页查询所有用户的饮食记录列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<DietRecordResponseDTO>>> getDietRecords(DietRecordQueryDTO queryDTO) {
        // 创建Command对象
        DietRecordQueryCommand command = new DietRecordQueryCommand();
        BeanUtils.copyProperties(queryDTO, command);

        PageResult<DietRecordResponseDTO> result = dietRecordService.getAllUsersDietRecords(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取单条饮食记录详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DietRecordResponseDTO>> getDietRecordDetail(@PathVariable("id") Long id) {
        DietRecordResponseDTO record = dietRecordService.getDietRecordDetail(id);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteDietRecord(@PathVariable("id") Long id) {
        // 管理员删除记录，不需要验证用户ID
        DietRecordDeleteCommand command = DietRecordDeleteCommand.of(null, id);
        boolean result = dietRecordService.deleteDietRecord(command);
        if (!result) {
            throw new BusinessException(400, "删除失败");
        }
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}