package com.example.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 头像响应DTO
 */
@Data
public class AvatarResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 上传URL
     */
    private String uploadUrl;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 创建一个上传URL响应
     * 
     * @param uploadUrl 上传URL
     * @param fileName 文件名
     * @return 新的响应对象
     */
    public static AvatarResponseDTO createUploadResponse(String uploadUrl, String fileName) {
        AvatarResponseDTO response = new AvatarResponseDTO();
        response.setUploadUrl(uploadUrl);
        response.setFileName(fileName);
        return response;
    }
    
    /**
     * 创建一个下载URL响应
     * 
     * @param avatarUrl 头像URL
     * @param fileName 文件名
     * @return 新的响应对象
     */
    public static AvatarResponseDTO createDownloadResponse(String avatarUrl, String fileName) {
        AvatarResponseDTO response = new AvatarResponseDTO();
        response.setAvatarUrl(avatarUrl);
        response.setFileName(fileName);
        return response;
    }
}
