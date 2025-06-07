package com.example.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoDTO implements Serializable {
    private Long id;
    private String username;
    private String role;
    private String email;     // 添加email字段，用户列表页面需要
    private Integer status;   // 添加status字段，用户列表页面需要
    private Date createTime;  // 添加createTime字段，用户列表页面需要

    // 可选字段，根据业务需求决定是否返回
    private String avatarUrl;    // 用户头像URL
    private String phone;     // 手机号（可选）

    // 敏感信息不返回
    // private String password;
    // private String openid;
}
