package com.example.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WechatLoginRequestDTO implements Serializable {
    private String code; // 微信登录时获取的code
    private String encryptedData; // 用户敏感数据
    private String iv; // 偏移向量
}
