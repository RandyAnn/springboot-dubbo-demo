package com.example.common.service;

import com.example.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口，用于处理文件上传和访问
 */
public interface FileService {

    /**
     * 生成文件上传的预签名URL
     *
     * @param id 对象ID（可以是用户ID、食物ID等）
     * @param fileType 文件类型（例如："avatar"表示用户头像，"foodimage"表示食物图片）
     * @param contentType 文件的内容类型（例如："image/jpeg"）
     * @param expiration URL的有效期（分钟）
     * @return 上传用的预签名URL
     * @throws BusinessException 生成预签名URL失败时抛出业务异常
     */
    String generateUploadPresignedUrl(Long id, String fileType, String contentType, int expiration) throws BusinessException;

    /**
     * 生成文件下载的预签名URL
     *
     * @param fileName 文件名
     * @param expiration URL的有效期（分钟）
     * @return 下载用的预签名URL
     * @throws BusinessException 生成预签名URL失败时抛出业务异常
     */
    String generateDownloadPresignedUrl(String fileName, int expiration) throws BusinessException;

    /**
     * 删除指定的文件
     *
     * @param fileName 要删除的文件名
     * @throws BusinessException 删除文件失败时抛出业务异常
     */
    void deleteFile(String fileName) throws BusinessException;
}