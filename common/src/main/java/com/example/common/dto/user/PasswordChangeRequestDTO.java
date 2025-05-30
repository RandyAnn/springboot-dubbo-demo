package com.example.common.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * 修改密码请求DTO
 */
@Data
public class PasswordChangeRequestDTO implements Serializable {
    private Long userId;
    private String oldPassword;
    private String newPassword;
    private String token; // 用于存储当前用户的JWT令牌，用于在修改密码后使其失效
}