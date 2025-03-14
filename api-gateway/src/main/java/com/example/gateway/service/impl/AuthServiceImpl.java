package com.example.gateway.service.impl;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.gateway.service.AuthService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    // 使用完全限定名引用远程服务
    @DubboReference
    private com.example.auth.service.AuthService remoteAuthService;

    @Override
    public LoginResponse login(LoginRequest request) {
        return remoteAuthService.login(request);
    }

    @Override
    public void logout(String token) {
        remoteAuthService.logout(token);
    }
}
