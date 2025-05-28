package com.example.gateway.controller;

import com.example.common.command.NutritionGoalCommand;
import com.example.common.command.UserUpdateCommand;
import com.example.common.dto.*;
import com.example.common.entity.UserNutritionGoal;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.service.UserNutritionGoalService;
import com.example.common.service.UserService;
import com.example.common.util.SecurityContextUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器，提供普通用户相关的接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @DubboReference
    private UserService userService;

    @DubboReference
    private UserNutritionGoalService userNutritionGoalService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        UserInfoDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户信息
     * Controller层只负责：
     * 1. 获取当前用户ID
     * 2. 参数校验和转换
     * 3. 调用Service层
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Boolean>> updateUserInfo(@RequestBody UserUpdateRequestDTO requestDTO) {
        // 1. 获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 2. 创建命令对象并转换参数
        UserUpdateCommand command = UserUpdateCommand.withUserId(userId);
        // 只复制前端传递的字段，null字段不会被复制
        BeanUtils.copyProperties(requestDTO, command);

        // 3. 调用Service层处理业务逻辑，直接返回boolean
        boolean success = userService.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    /**
     * 获取用户营养目标
     */
    @GetMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> getNutritionGoal() {
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 使用新的方法，它会处理null情况并返回默认值
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getOrCreateNutritionGoalByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(nutritionGoal));
    }

    /**
     * 更新用户营养目标
     */
    @PutMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> updateNutritionGoal(@RequestBody NutritionGoalRequestDTO requestDTO) {
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建命令对象
        NutritionGoalCommand command = NutritionGoalCommand.withUserId(userId);

        // 复制请求数据到命令对象
        BeanUtils.copyProperties(requestDTO, command);

        // 直接使用命令对象调用服务方法
        UserNutritionGoal updatedGoal = userNutritionGoalService.saveOrUpdateNutritionGoal(command);

        return ResponseEntity.ok(ApiResponse.success(updatedGoal));
    }

    /**
     * 生成头像上传URL
     * Controller层只负责：
     * 1. 获取当前用户ID
     * 2. 参数校验
     * 3. 调用Service层
     */
    @PostMapping("/avatar/upload-url")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> generateAvatarUploadUrl(
            @RequestParam("contentType") String contentType) {
        try {
            // 1. 获取当前用户ID
            Long userId = SecurityContextUtil.getCurrentUserId();

            // 2. 调用Service层处理业务逻辑
            AvatarResponseDTO response = userService.generateAvatarUploadUrl(userId, contentType);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    /**
     * 获取用户头像URL
     * Controller层只负责：
     * 1. 获取当前用户ID
     * 2. 调用Service层
     */
    @GetMapping("/avatar")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> generateAvatarDownloadUrl() {
        try {
            // 1. 获取当前用户ID
            Long userId = SecurityContextUtil.getCurrentUserId();

            // 2. 调用Service层处理业务逻辑
            AvatarResponseDTO response = userService.generateAvatarDownloadUrl(userId);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }
}