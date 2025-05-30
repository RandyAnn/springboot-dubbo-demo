package com.example.gateway.controller;

import com.example.auth.service.AuthService;
import com.example.common.command.UserCreateCommand;
import com.example.common.dto.LoginRequestDTO;
import com.example.common.dto.LoginResponseDTO;
import com.example.common.dto.PasswordChangeDTO;
import com.example.common.dto.UserInfoDTO;
import com.example.common.dto.WechatLoginRequestDTO;
import com.example.common.entity.User;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.service.UserService;
import com.example.common.util.SecurityContextUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @DubboReference
    private AuthService remoteAuthService;

    @DubboReference
    private UserService userService;

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> adminLogin(@RequestBody LoginRequestDTO request) {
        request.setLoginType(LoginRequestDTO.LoginType.ADMIN);
        LoginResponseDTO response = remoteAuthService.adminLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> userLogin(@RequestBody LoginRequestDTO request) {
        request.setLoginType(LoginRequestDTO.LoginType.USER);
        LoginResponseDTO response = remoteAuthService.userLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/wechat-login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> wechatLogin(@RequestBody WechatLoginRequestDTO request) {
        LoginResponseDTO response = remoteAuthService.wechatLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoDTO>> register(@RequestBody User user) {
        // 创建UserCreateCommand对象并复制属性
        UserCreateCommand command = new UserCreateCommand();
        BeanUtils.copyProperties(user, command);

        // 调用服务创建用户
        UserInfoDTO createdUser = userService.createUser(command);
        return ResponseEntity.ok(ApiResponse.success(createdUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            remoteAuthService.logout(token);
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        // 无效请求时直接抛出异常，由全局异常处理器处理
        throw new BusinessException("无效的请求");
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser() {
        // 从Spring Security上下文中获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }

        // 获取用户信息
        UserInfoDTO userInfo = userService.getUserById(userId);
        if (userInfo == null) {
            throw new BusinessException(404, "用户不存在");
        }

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PasswordChangeDTO request) {

        // 从Spring Security上下文中获取当前用户ID
        Long userId = SecurityContextUtil.getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }

        // 提取token用于在密码修改成功后使其失效
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 设置用户ID和token
        request.setUserId(userId);
        request.setToken(token);

        // 调用修改密码服务
        boolean result = remoteAuthService.changePassword(request);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

}

