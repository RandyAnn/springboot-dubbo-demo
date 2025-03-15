package com.example.gateway.controller;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.common.entity.User;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @DubboReference
    private com.example.auth.service.AuthService remoteAuthService;

    @DubboReference
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = remoteAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        User createdUser = userService.createUser(user);
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
}

