package com.example.gateway.config.security;

import com.example.gateway.filter.JwtAuthenticationFilter;
import com.example.gateway.filter.SentinelFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Spring Security安全配置类
 * 负责配置安全过滤器链、认证授权规则等
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SentinelFilter sentinelFilter;

    /**
     * 配置安全过滤器链
     * 定义认证授权规则和过滤器执行顺序
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors() // 启用CORS
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 允许未登录访问的路径
                .antMatchers("/api/auth/admin/login", "/api/auth/user/login","/api/auth/register","/api/auth/wechat-login").permitAll()
                // 允许对文件的匿名访问
                .antMatchers("/api/files/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(sentinelFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, SentinelFilter.class)
                .build();
    }

    /**
     * 密码编码器Bean
     * 用于密码加密和验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
