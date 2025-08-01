package com.example.auth.service;

import com.example.user.dto.LoginRequestDTO;
import com.example.user.dto.LoginResponseDTO;
import com.example.user.dto.PasswordChangeRequestDTO;
import com.example.user.dto.WechatLoginRequestDTO;
import com.example.shared.exception.BusinessException;

public interface AuthService {
//    LoginResponse login(LoginRequest request) throws BusinessException;

    /**
     * 管理员登录
     */
    LoginResponseDTO adminLogin(LoginRequestDTO request) throws BusinessException;

    /**
     * 普通用户登录
     */
    LoginResponseDTO userLogin(LoginRequestDTO request) throws BusinessException;

    LoginResponseDTO wechatLogin(WechatLoginRequestDTO request) throws BusinessException;
    void logout(String token);

    /**
     * 修改用户密码
     *
     * @param request 密码修改请求DTO
     * @return 是否修改成功
     * @throws BusinessException 修改失败时抛出业务异常
     */
    boolean changePassword(PasswordChangeRequestDTO request) throws BusinessException;
}