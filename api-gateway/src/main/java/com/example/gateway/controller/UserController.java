package com.example.gateway.controller;

import com.example.common.command.NutritionGoalCommand;
import com.example.common.command.UserUpdateCommand;
import com.example.common.dto.*;
import com.example.common.entity.UserNutritionGoal;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.service.FileService;
import com.example.common.service.UserNutritionGoalService;
import com.example.common.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @DubboReference
    private FileService fileService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserInfoDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserInfoDTO>> updateUserInfo(@RequestBody UserUpdateRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserInfoDTO currentUser = userService.getUserByUsername(username);

        // 创建命令对象
        UserUpdateCommand command = UserUpdateCommand.withUserId(currentUser.getId());

        // 只复制允许修改的字段
        BeanUtils.copyProperties(requestDTO, command);

        // 防止修改敏感字段
        command.setUsername(currentUser.getUsername());
        command.setEmail(currentUser.getEmail());
        command.setRole(currentUser.getRole());
        command.setStatus(currentUser.getStatus());

        // 直接使用命令对象调用服务方法
        UserInfoDTO updatedUser = userService.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    /**
     * 获取用户营养目标
     */
    @GetMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> getNutritionGoal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserInfoDTO user = userService.getUserByUsername(username);

        // 使用新的方法，它会处理null情况并返回默认值
        UserNutritionGoal nutritionGoal = userNutritionGoalService.getOrCreateNutritionGoalByUserId(user.getId());

        return ResponseEntity.ok(ApiResponse.success(nutritionGoal));
    }

    /**
     * 更新用户营养目标
     */
    @PutMapping("/nutrition-goal")
    public ResponseEntity<ApiResponse<UserNutritionGoal>> updateNutritionGoal(@RequestBody NutritionGoalRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserInfoDTO user = userService.getUserByUsername(username);

        // 创建命令对象
        NutritionGoalCommand command = NutritionGoalCommand.withUserId(user.getId());

        // 复制请求数据到命令对象
        BeanUtils.copyProperties(requestDTO, command);

        // 直接使用命令对象调用服务方法
        UserNutritionGoal updatedGoal = userNutritionGoalService.saveOrUpdateNutritionGoal(command);

        return ResponseEntity.ok(ApiResponse.success(updatedGoal));
    }

    /**
     * 生成头像上传URL
     *
     * 改为生成预签名URL让客户端直接上传到对象存储
     */
    @PostMapping("/avatar/upload-url")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> generateAvatarUploadUrl(
            @RequestParam("contentType") String contentType) {
        try {
            // 从认证对象中获取userId
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            Long userId = (Long) details.get("userId");

            // 生成上传URL（有效期15分钟）
            String presignedUrlWithFilename = fileService.generateUploadPresignedUrl(
                    userId, "avatar", contentType, 15);

            // 分离URL和文件名
            String[] parts = presignedUrlWithFilename.split(":::");
            String presignedUrl = parts[0];
            String fileName = parts[1];

            // 更新用户头像文件名（此时还未上传完成，但我们已经知道文件名了）
            userService.updateUserAvatar(userId, fileName);

            // 构建响应
            AvatarResponseDTO response = AvatarResponseDTO.createUploadResponse(presignedUrl, fileName);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    /**
     * 获取用户头像URL
     *
     * 改为生成预签名的下载URL
     */
    @GetMapping("/avatar")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> getUserAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 从认证对象中获取userId
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        // 获取用户信息
        UserInfoDTO user = userService.getUserById(userId);

        AvatarResponseDTO response;

        // 如果用户有头像，则生成下载URL
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                // 生成下载URL（有效期60分钟）
                String downloadUrl = fileService.generateDownloadPresignedUrl(user.getAvatarUrl(), 60);
                response = AvatarResponseDTO.createDownloadResponse(downloadUrl, user.getAvatarUrl());
            } catch (BusinessException e) {
                // 如果生成URL失败，返回空URL
                response = AvatarResponseDTO.createDownloadResponse("", "");
            }
        } else {
            // 如果用户没有头像，则返回空URL
            response = AvatarResponseDTO.createDownloadResponse("", "");
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}