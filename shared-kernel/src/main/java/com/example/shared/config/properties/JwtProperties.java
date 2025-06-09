package com.example.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT配置属性类
 * 统一管理JWT相关的所有配置项
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT密钥
     */
    private String secret;
    
    /**
     * JWT过期时间（毫秒）
     */
    private long expiration = 86400000; // 默认24小时
}
