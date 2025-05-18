package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 头像上传请求DTO
 */
@Data
public class AvatarUploadRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件内容类型
     */
    private String contentType;
}
