package com.example.gateway.logging.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求日志过滤器
 * 负责记录所有通过网关的请求和响应信息
 * 提供分布式追踪支持
 */
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_ATTRIBUTE = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 生成或获取traceId
        String traceId = getOrGenerateTraceId(request);

        // 设置MDC用于日志输出
        MDC.put("traceId", traceId);

        // 将traceId存储到exchange attributes中
        exchange.getAttributes().put(TRACE_ID_ATTRIBUTE, traceId);

        // 记录请求开始
        long startTime = System.currentTimeMillis();
        logRequest(request, traceId);

        // 添加traceId到响应头
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(TRACE_ID_HEADER, traceId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // 记录请求结束
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    logResponse(request, response, traceId, duration);

                    // 清理MDC
                    MDC.clear();
                });
    }

    /**
     * 获取或生成traceId
     */
    private String getOrGenerateTraceId(ServerHttpRequest request) {
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        return traceId;
    }

    /**
     * 记录请求信息
     */
    private void logRequest(ServerHttpRequest request, String traceId) {
        String method = request.getMethod().name();
        String uri = request.getURI().toString();
        String remoteAddress = getClientIpAddress(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");

        log.info("Request started - Method: {}, URI: {}, RemoteAddr: {}, UserAgent: {}, TraceId: {}",
                method, uri, remoteAddress, userAgent, traceId);
    }

    /**
     * 记录响应信息
     */
    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, String traceId, long duration) {
        String method = request.getMethod().name();
        String uri = request.getURI().getPath();
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

        log.info("Request completed - Method: {}, URI: {}, Status: {}, Duration: {}ms, TraceId: {}",
                method, uri, statusCode, duration, traceId);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return 100; // 在认证授权之后执行，记录完整的用户信息
    }
}
