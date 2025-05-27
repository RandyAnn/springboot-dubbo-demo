package com.example.common.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户创建命令对象
 */
@Data
public class UserCreateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private String avatarUrl;
    private String password;
    private String openid;
}
