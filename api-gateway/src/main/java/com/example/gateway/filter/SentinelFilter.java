package com.example.gateway.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;

import com.example.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sentinel限流过滤器
 * 在认证之前执行限流检查，确保限流优先级高于认证
 */
@Component
@Order(1) // 确保在其他过滤器之前执行
public class SentinelFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SentinelFilter.class);

    @Autowired
    @Qualifier("objectMapper") // 复用项目的主要ObjectMapper
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String resource = method + ":" + uri;

        // 根据URI路径确定资源名称
        String sentinelResource = getSentinelResource(uri);

        Entry entry = null;
        try {
            // 进入Sentinel保护
            entry = SphU.entry(sentinelResource);

            // 继续执行后续过滤器
            filterChain.doFilter(request, response);

        } catch (BlockException ex) {
            // 在Filter中直接处理限流异常，使用项目统一的ApiResponse格式
            handleBlockException(response, ex);
            return; // 不继续执行后续过滤器
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * 根据URI路径确定Sentinel资源名称
     */
    private String getSentinelResource(String uri) {
        if (uri.startsWith("/api/auth/")) {
            return "/api/auth/**";
        } else if (uri.startsWith("/api/users/")) {
            return "/api/users/**";
        } else if (uri.startsWith("/api/foods/")) {
            return "/api/foods/**";
        } else if (uri.startsWith("/api/diet/")) {
            return "/api/diet/**";
        } else if (uri.startsWith("/api/nutrition/")) {
            return "/api/nutrition/**";
        } else if (uri.startsWith("/api/files/")) {
            return "/api/files/**";
        } else {
            return uri;
        }
    }

    /**
     * 处理限流异常，根据不同异常类型返回相应的HTTP状态码和错误信息
     */
    private void handleBlockException(HttpServletResponse response, BlockException ex) throws IOException {
        int statusCode;
        String message;
        String exceptionType = ex.getClass().getSimpleName();

        if (ex instanceof FlowException) {
            statusCode = 429;
            message = "请求过于频繁，请稍后再试";
        } else if (ex instanceof DegradeException) {
            statusCode = 503;
            message = "服务暂时不可用，请稍后再试";
        } else if (ex instanceof ParamFlowException) {
            statusCode = 429;
            message = "参数访问过于频繁，请稍后再试";
        } else if (ex instanceof AuthorityException) {
            statusCode = 403;
            message = "访问权限不足";
        } else {
            statusCode = 429;
            message = "请求被限制，请稍后再试";
        }

        log.warn("Sentinel blocked request: uri={}, exceptionType={}, statusCode={}, message={}",
            getCurrentRequestUri(), exceptionType, statusCode, message);

        ApiResponse<Object> apiResponse = ApiResponse.error(statusCode, message);

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 获取当前请求URI（线程安全）
     * @return 当前请求URI，如果无法获取则返回"unknown"
     */
    private String getCurrentRequestUri() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
