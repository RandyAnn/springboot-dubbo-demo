package com.example.gateway.config.security;

import com.example.gateway.filter.JwtAuthenticationFilter;
import com.example.gateway.filter.SentinelFilter;
import com.example.shared.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * 统一管理所有过滤器Bean的创建和配置
 */
@Configuration
public class FilterConfig {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * JWT认证过滤器Bean
     * 负责JWT token的验证和用户认证信息的设置
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    /**
     * Sentinel限流过滤器Bean
     * 负责API请求的限流和熔断保护
     */
    @Bean
    public SentinelFilter sentinelFilter() {
        return new SentinelFilter();
    }
}
