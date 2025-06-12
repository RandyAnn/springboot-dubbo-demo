package com.example.gateway.logging.config;

import com.example.gateway.logging.filter.RequestLoggingFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 统一日志配置类
 * 负责配置Gateway的日志记录功能
 */
@Configuration
public class LoggingConfig {

    /**
     * 请求日志过滤器
     * 记录所有通过网关的请求和响应信息
     * 在认证和授权之后执行，记录完整的请求信息
     */
    @Bean
    @Order(100) // 在认证授权之后执行，记录完整的用户信息
    public GlobalFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }
}
