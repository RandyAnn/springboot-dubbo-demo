package com.example.gateway.controller;

import com.example.common.command.DietRecordDeleteCommand;
import com.example.common.command.DietRecordQueryCommand;
import com.example.common.dto.DietRecordQueryDTO;
import com.example.common.dto.DietRecordResponseDTO;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.DietRecordService;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<PageResult<DietRecordResponseDTO>>> getDietRecords(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "mealType", required = false) String mealType) {

        // 创建Command对象
        DietRecordQueryCommand command = new DietRecordQueryCommand();
        command.setPage(page);
        command.setSize(size);
        command.setUserId(userId);
        command.setStartDate(startDate);
        command.setEndDate(endDate);
        command.setMealType(mealType);

        PageResult<DietRecordResponseDTO> result = dietRecordService.getAllUsersDietRecords(command);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取单条饮食记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DietRecordResponseDTO>> getDietRecordDetail(@PathVariable("id") Long id) {
        DietRecordResponseDTO record = dietRecordService.getDietRecordDetail(id);
        if (record == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteDietRecord(@PathVariable("id") Long id) {
        // 管理员删除记录，不需要验证用户ID
        DietRecordDeleteCommand command = DietRecordDeleteCommand.of(null, id);
        boolean result = dietRecordService.deleteDietRecord(command);
        if (!result) {
            return ResponseEntity.ok(ApiResponse.error(400, "删除失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}