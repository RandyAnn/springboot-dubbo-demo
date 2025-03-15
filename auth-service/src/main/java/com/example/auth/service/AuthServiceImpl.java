package com.example.auth.service;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.common.entity.User;
import com.example.common.exception.BusinessException;
import com.example.common.service.UserService;
import com.example.common.util.JwtUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@DubboService
public class AuthServiceImpl implements AuthService {

    @DubboReference
    private UserService userService;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userService.getUserByUsername(request.getUsername());
//        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new RuntimeException("Invalid username or password");
//        }

        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new BusinessException(401,"用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(claims, user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());

        return response;
    }

    @Override
    public void logout(String token) {
        jwtUtil.blacklistToken(token);
    }
}
