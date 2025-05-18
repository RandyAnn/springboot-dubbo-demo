package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户状态更新DTO
 */
@Data
public class UserStatusUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户状态
     */
    private Integer status;
}
