package com.example.gateway.filter;

import com.example.shared.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 * 负责验证JWT token并设置Spring Security认证上下文
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            // 使用JwtUtil验证token（包含黑名单检查）
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);

                String username = claims.getSubject();
                String role = (String) claims.get("role");
                // 从JWT中获取userId
                Long userId = Long.valueOf(claims.get("userId").toString());

                // 确保角色有ROLE_前缀，但不要重复添加
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                // 创建用户详情对象，保存额外信息
                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("role", role);

                // 创建认证令牌 - 使用userId作为principal
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix)));

                // 设置认证详情
                authentication.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
