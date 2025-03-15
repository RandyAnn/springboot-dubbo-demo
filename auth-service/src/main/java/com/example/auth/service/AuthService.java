package com.example.auth.service;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.common.exception.BusinessException;

public interface AuthService {
    LoginResponse login(LoginRequest request) throws BusinessException;
    void logout(String token);
}