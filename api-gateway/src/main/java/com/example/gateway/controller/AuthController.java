package com.example.gateway.controller;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.common.entity.User;
import com.example.common.service.UserService;
import com.example.gateway.service.AuthService;
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = remoteAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            remoteAuthService.logout(token);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
