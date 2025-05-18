package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponse implements Serializable {
    private String token;
    private UserInfoDTO userInfo;  // 使用 DTO 替代 User 实体
}
