package com.example.gateway.controller;

import com.example.common.command.UserCreateCommand;
import com.example.common.command.UserPageQueryCommand;
import com.example.common.command.UserUpdateCommand;
import com.example.common.dto.*;
import com.example.common.entity.User;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.FileService;
import com.example.common.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @DubboReference
    private UserService userService;

    @DubboReference
    private FileService fileService;

    /**
     * 分页查询用户接口，只有拥有 ADMIN 角色的用户可以访问。
     * 返回UserInfoDTO对象，只包含前端需要的字段，不包含敏感信息。
     * 用户头像URL的处理已移至服务层。
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<UserInfoDTO>>> listUsers(UserQueryDTO queryDTO) {
        try {
            // 创建查询命令对象
            UserPageQueryCommand command = new UserPageQueryCommand();
            BeanUtils.copyProperties(queryDTO, command);

            // 获取用户分页数据（已在服务层处理头像URL）
            PageResult<UserInfoDTO> result = userService.getUserInfoPage(command);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分页查询用户失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "分页查询用户失败"));
        }
    }

    /**
     * 添加新用户接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> addUser(@RequestBody UserCreateRequestDTO requestDTO) {
        try {
            // 创建命令对象
            UserCreateCommand command = new UserCreateCommand();
            BeanUtils.copyProperties(requestDTO, command);

            // 强制角色为普通用户
            command.setRole("USER");

            // 直接使用命令对象调用服务方法
            UserInfoDTO createdUser = userService.createUser(command);
            return ResponseEntity.ok(ApiResponse.success(createdUser));
        } catch (Exception e) {
            log.error("添加新用户失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "添加新用户失败"));
        }
    }

    /**
     * 更新用户信息接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDTO requestDTO) {
        try {
            // 创建命令对象
            UserUpdateCommand command = UserUpdateCommand.withUserId(userId);

            // 复制请求数据到命令对象
            BeanUtils.copyProperties(requestDTO, command);

            // 调用服务方法更新用户
            boolean success = userService.updateUser(command);
            if (!success) {
                return ResponseEntity.status(500).body(ApiResponse.error(500, "更新用户信息失败"));
            }

            // 管理员接口需要返回完整用户信息，所以查询一次
            UserInfoDTO updatedUser = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (Exception e) {
            log.error("更新用户信息失败，用户ID: {}", userId, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新用户信息失败"));
        }
    }

    /**
     * 更新用户状态接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateDTO requestDTO) {
        try {
            boolean result = userService.updateUserStatus(userId, requestDTO.getStatus());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("更新用户状态失败，用户ID: {}", userId, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新用户状态失败"));
        }
    }

    /**
     * 获取用户详情接口，只有拥有 ADMIN 角色的用户可以访问
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserDetail(@PathVariable Long userId) {
        try {
            UserInfoDTO userInfo = userService.getUserById(userId);
            if (userInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(userInfo));
        } catch (Exception e) {
            log.error("获取用户详情失败，用户ID: {}", userId, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取用户详情失败"));
        }
    }
}


