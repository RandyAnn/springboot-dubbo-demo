package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponse implements Serializable {
    private String token;
    private String username;
    private String role;
}
