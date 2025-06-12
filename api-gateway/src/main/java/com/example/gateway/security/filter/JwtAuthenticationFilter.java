package com.example.gateway.security.filter;

import com.example.shared.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 * 负责验证JWT token并设置Spring Security认证上下文
 * 适配WebFlux环境，使用ServerWebExchange替代HttpServletRequest
 * 作为WebFilter集成到Spring Security的过滤器链中
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 获取Authorization头
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 如果没有Authorization header，或者不是Bearer Token，则直接跳过
        // 后续的Spring Security链会根据路径决定是否需要认证
        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
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

                // 创建SecurityContext
                SecurityContext securityContext = new SecurityContextImpl(authentication);

                // 添加用户信息到请求Header中
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId.toString())
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                        .build();

                // 创建新的exchange
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build();

                // 将用户上下文信息存储到exchange的attributes中
                mutatedExchange.getAttributes().put("userId", userId);
                mutatedExchange.getAttributes().put("username", username);
                mutatedExchange.getAttributes().put("userRole", role);

                // 使用ReactiveSecurityContextHolder设置安全上下文，并继续过滤器链
                return chain.filter(mutatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            } else {
                // token验证失败，继续过滤器链（让Spring Security处理未认证情况）
                return chain.filter(exchange);
            }

        } catch (Exception e) {
            // token解析异常，继续过滤器链（让Spring Security处理未认证情况）
            return chain.filter(exchange);
        }
    }
} 