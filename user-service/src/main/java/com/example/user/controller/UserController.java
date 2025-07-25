package com.example.user.controller;

import com.example.user.command.NutritionGoalCommand;
import com.example.user.command.UserUpdateCommand;
import com.example.user.dto.*;
import com.example.shared.response.ApiResponse;
import com.example.user.service.UserNutritionGoalService;
import com.example.user.service.UserService;
import com.example.shared.util.SecurityContextUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器，提供普通用户相关的接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
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
    public ResponseEntity<ApiResponse<UserNutritionGoalResponseDTO>> getNutritionGoal() {
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 获取用户营养目标，如果不存在则创建默认值
        UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoalService.getNutritionGoal(userId);

        return ResponseEntity.ok(ApiResponse.success(nutritionGoal));
    }

    /**
     * 更新用户营养目标
     */
    @PutMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<Boolean>> updateNutritionGoal(@RequestBody NutritionGoalRequestDTO requestDTO) {
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 创建命令对象
        NutritionGoalCommand command = NutritionGoalCommand.withUserId(userId);

        // 复制请求数据到命令对象
        BeanUtils.copyProperties(requestDTO, command);

        // 直接使用命令对象调用服务方法
        boolean success = userNutritionGoalService.UpdateNutritionGoal(command);

        return ResponseEntity.ok(ApiResponse.success(success));
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
        // 1. 获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 2. 调用Service层处理业务逻辑
        AvatarResponseDTO response = userService.generateAvatarUploadUrl(userId, contentType);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户头像URL
     * Controller层只负责：
     * 1. 获取当前用户ID
     * 2. 调用Service层生成预签名URL
     */
    @GetMapping("/avatar")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> getAvatarUrl() {
        // 1. 获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();

        // 2. 调用Service层生成预签名下载URL
        AvatarResponseDTO response = userService.generateAvatarDownloadUrl(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
