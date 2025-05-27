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
import com.example.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @DubboReference
    private AuthService remoteAuthService;

    @DubboReference
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
//        LoginResponse response = remoteAuthService.login(request);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> adminLogin(@RequestBody LoginRequestDTO request) {
        request.setLoginType(LoginRequestDTO.LoginType.ADMIN);
        LoginResponseDTO response = remoteAuthService.adminLogin(request);
        // AuthService现在已经返回包含UserInfoDTO的LoginResponseDTO
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> userLogin(@RequestBody LoginRequestDTO request) {
        request.setLoginType(LoginRequestDTO.LoginType.USER);
        LoginResponseDTO response = remoteAuthService.userLogin(request);
        // AuthService现在已经返回包含UserInfoDTO的LoginResponseDTO
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/wechat-login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> wechatLogin(@RequestBody WechatLoginRequestDTO request) {
        LoginResponseDTO response = remoteAuthService.wechatLogin(request);
        // AuthService现在已经返回包含UserInfoDTO的LoginResponseDTO
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoDTO>> register(@RequestBody User user) {
        // 创建UserCreateCommand对象
        UserCreateCommand command = new UserCreateCommand();

        // 复制User对象的属性到UserCreateCommand对象
        command.setUsername(user.getUsername());
        command.setPassword(user.getPassword());
        command.setEmail(user.getEmail());
        command.setRole(user.getRole());
        command.setStatus(user.getStatus());
        command.setAvatarUrl(user.getAvatarUrl());
        command.setOpenid(user.getOpenid());

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
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未授权");
        }

        // 从JWT中提取用户ID
        String token = authHeader.substring(7);
        Long userId = extractUserIdFromToken(token);

        if (userId == null) {
            throw new BusinessException(401, "无效的token");
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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未授权");
        }

        // 从JWT中提取用户ID
        String token = authHeader.substring(7);
        Long userId = extractUserIdFromToken(token);

        if (userId == null) {
            throw new BusinessException(401, "无效的token");
        }

        // 设置用户ID和token
        request.setUserId(userId);
        request.setToken(token); // 设置token，用于在密码修改成功后使其失效

        // 调用修改密码服务
        boolean result = remoteAuthService.changePassword(request);

        // 如果密码修改成功，返回204状态码，表示用户需要重新登录
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
    }

    // 从token中提取用户ID的方法
    private Long extractUserIdFromToken(String token) {
        try {
            // 使用JwtUtil解析token
            Claims claims = jwtUtil.parseToken(token);

            // 从claims中获取userId
            // 首先尝试获取userId字段
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    return Long.parseLong((String) userIdObj);
                }
            }

            // 如果userId字段不存在，则返回null
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

