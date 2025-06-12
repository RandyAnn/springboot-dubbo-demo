package com.example.shared.config.security;

import com.example.shared.config.properties.PasswordPolicyProperties;
import com.example.shared.filter.UserContextFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 共享安全配置类
 * 统一管理安全相关的配置和Bean，包括密码策略等安全组件
 *
 * 注意：限流配置已迁移到api-gateway模块的GatewayProperties中
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
     * 统一的密码编码器Bean
     * 使用BCrypt算法进行密码加密，只有在没有其他PasswordEncoder Bean时才创建
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 统一的用户上下文过滤器Bean
     * 用于微服务间的安全上下文传递，只有在没有其他UserContextFilter Bean时才创建
     */
    @Bean
    @ConditionalOnMissingBean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }
}
