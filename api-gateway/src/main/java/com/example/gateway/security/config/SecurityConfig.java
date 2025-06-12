package com.example.gateway.security.config;

import com.example.gateway.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 安全策略配置类
 * 负责配置WebFlux安全策略，定义路径访问权限
 * 统一管理不需要认证的路径和需要认证的路径
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置WebFlux安全过滤器链
     * 明确配置每个路径的访问权限，实现统一的安全策略管理
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                // 禁用CSRF保护
                .csrf().disable()
                // 禁用表单登录
                .formLogin().disable()
                // 禁用HTTP Basic认证
                .httpBasic().disable()
                // 配置路径访问权限 - 明确指定哪些路径需要认证，哪些不需要
                .authorizeExchange(exchanges -> exchanges
                        // CORS预检请求不需要认证
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 认证相关接口不需要JWT认证（用于登录、注册等）
                        .pathMatchers("/api/auth/**").permitAll()
                        // 文件下载接口不需要认证（通过预签名URL控制访问）
                        .pathMatchers("/api/files/download/**").permitAll()
                        // 静态资源不需要认证
                        .pathMatchers("/", "/static/**", "/public/**").permitAll()
                        // 健康检查接口不需要认证
                        .pathMatchers("/actuator/**").permitAll()
                        // 其他API接口需要认证
                        .pathMatchers("/api/**").authenticated()
                        // 其他路径允许访问
                        .anyExchange().permitAll()
                )
                .build();
    }
}
