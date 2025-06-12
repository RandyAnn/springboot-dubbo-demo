package com.example.shared.filter;

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
 * 用户上下文过滤器
 * 负责从HTTP Header中读取用户信息并重建Spring Security认证上下文
 * 用于微服务间的安全上下文传递
 */
public class UserContextFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从Header中读取用户信息
        String userIdStr = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String role = request.getHeader(HEADER_USER_ROLE);

        // 如果存在用户信息，重建SecurityContext
        if (userIdStr != null && username != null && role != null) {
            try {
                Long userId = Long.valueOf(userIdStr);

                // 确保角色有ROLE_前缀，但不要重复添加
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                // 创建用户详情对象，保存额外信息（复用现有SecurityContextUtil的模式）
                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("role", role);

                // 创建认证令牌 - 使用userId作为principal（与JwtAuthenticationFilter保持一致）
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix)));

                // 设置认证详情
                authentication.setDetails(details);

                // 设置到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (NumberFormatException e) {
                // 如果userId格式错误，清除上下文
                SecurityContextHolder.clearContext();
            }
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
