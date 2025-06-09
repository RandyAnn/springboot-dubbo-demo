package com.example.shared.config.jwt;

import com.example.shared.config.properties.JwtProperties;
import com.example.shared.util.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class SharedJwtConfig {

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
}
