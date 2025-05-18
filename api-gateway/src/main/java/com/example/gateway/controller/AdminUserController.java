package com.example.gateway.controller;

import com.example.common.command.UserUpdateCommand;
import com.example.common.dto.*;
import com.example.common.entity.User;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.FileService;
import com.example.common.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        // 获取用户分页数据（已在服务层处理头像URL）
        PageResult<UserInfoDTO> result = userService.getUserInfoPage(
                queryDTO.getPage(),
                queryDTO.getSize(),
                queryDTO.getStatus(),
                queryDTO.getKeyword(),
                queryDTO.getTimeFilter());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 添加新用户接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> addUser(@RequestBody UserUpdateRequestDTO requestDTO) {
        // 创建命令对象
        UserUpdateCommand command = new UserUpdateCommand();
        BeanUtils.copyProperties(requestDTO, command);

        // 强制角色为普通用户
        command.setRole("USER");

        // 直接使用命令对象调用服务方法
        UserInfoDTO createdUser = userService.createUser(command);
        return ResponseEntity.ok(ApiResponse.success(createdUser));
    }

    /**
     * 更新用户信息接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDTO requestDTO) {
        // 创建命令对象
        UserUpdateCommand command = UserUpdateCommand.withUserId(userId);

        // 复制请求数据到命令对象
        BeanUtils.copyProperties(requestDTO, command);

        // 直接使用命令对象调用服务方法
        UserInfoDTO updatedUser = userService.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    /**
     * 更新用户状态接口，只有拥有 ADMIN 角色的用户可以访问。
     */
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateDTO requestDTO) {
        boolean result = userService.updateUserStatus(userId, requestDTO.getStatus());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取用户详情接口，只有拥有 ADMIN 角色的用户可以访问
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserDetail(@PathVariable Long userId) {
        UserInfoDTO userInfo = userService.getUserById(userId);
        if (userInfo == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}


