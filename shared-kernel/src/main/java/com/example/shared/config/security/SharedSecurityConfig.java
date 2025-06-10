package com.example.shared.config.security;

import com.example.shared.config.properties.PasswordPolicyProperties;
import com.example.shared.config.properties.RateLimitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 共享安全配置类
 * 统一管理安全相关的配置和Bean，包括密码策略等安全组件
 */
@Configuration
public class SharedSecurityConfig {

    /**
     * 密码策略配置属性Bean
     * 绑定app.security.password前缀的配置项
     */
    @Bean
    @ConfigurationProperties(prefix = "app.security.password")
    public PasswordPolicyProperties passwordPolicyProperties() {
        return new PasswordPolicyProperties();
    }

    /**
     * 限流配置属性Bean
     * 绑定app.security.rate-limit前缀的配置项
     */
    @Bean
    @ConfigurationProperties(prefix = "app.security.rate-limit")
    public RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }
}
