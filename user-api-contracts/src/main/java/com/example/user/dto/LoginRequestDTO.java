package com.example.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginRequestDTO implements Serializable {
    private String username;
    private String email;
    private String password;
    private LoginType loginType;

    public enum LoginType {
        ADMIN, USER
    }
}
