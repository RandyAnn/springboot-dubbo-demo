package com.example.gateway.service;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String token);
}
