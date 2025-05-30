package com.example.common.config.jwt;

import com.example.common.util.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CommonJwtConfig {

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        return new JwtUtil(jwtProperties.getSecret(), jwtProperties.getExpiration(), redisTemplate);
    }

    @lombok.Data
    public static class JwtProperties {
        private String secret;
        private long expiration = 86400000; // 默认值
    }
}