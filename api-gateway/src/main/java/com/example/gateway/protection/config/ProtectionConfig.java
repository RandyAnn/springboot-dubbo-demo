package com.example.gateway.protection.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 流量保护配置类
 * 专门负责流量控制、熔断降级、系统保护
 * 使用Sentinel实现高级流量保护功能
 */
@Configuration
public class ProtectionConfig {

    private static final Logger log = LoggerFactory.getLogger(ProtectionConfig.class);

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public ProtectionConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                           ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * Sentinel Gateway过滤器
     * 负责流量控制和熔断降级
     * 最高优先级，在所有其他过滤器之前执行
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * Sentinel异常处理器
     * 处理限流和熔断时的响应
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 初始化Sentinel规则和回调
     */
    @PostConstruct
    public void initSentinel() {
        // 初始化限流规则
        initGatewayRules();

        // 初始化限流回调
        initBlockHandler();

        log.info("Sentinel Gateway protection initialized successfully");
    }

    /**
     * 初始化Gateway限流规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 认证服务限流 - 相对宽松，避免影响登录
        rules.add(createFlowRule("auth_route", 30));

        // 用户服务限流
        rules.add(createFlowRule("user_route", 50));
        rules.add(createFlowRule("admin_user_route", 20));

        // 食物服务限流 - 查询频繁，限制适中
        rules.add(createFlowRule("food_route", 100));
        rules.add(createFlowRule("admin_food_route", 30));

        // 饮食记录服务限流
        rules.add(createFlowRule("diet_records_route", 60));
        rules.add(createFlowRule("admin_diet_records_route", 25));

        // 营养分析服务限流 - 计算密集，限制较严
        rules.add(createFlowRule("nutrition_route", 40));
        rules.add(createFlowRule("health_route", 20));
        rules.add(createFlowRule("admin_nutrition_route", 15));

        // 文件服务限流 - 上传下载，限制适中
        rules.add(createFlowRule("files_route", 80));
        rules.add(createFlowRule("admin_files_route", 30));

        // 仪表盘服务限流 - 管理功能，限制较严
        rules.add(createFlowRule("admin_dashboard_route", 20));

        GatewayRuleManager.loadRules(rules);
        log.info("Gateway flow rules loaded, total rules: {}", rules.size());
    }

    /**
     * 创建限流规则
     */
    private GatewayFlowRule createFlowRule(String resource, double count) {
        GatewayFlowRule rule = new GatewayFlowRule(resource);
        rule.setGrade(1); // QPS限流
        rule.setCount(count);
        rule.setIntervalSec(1);
        rule.setControlBehavior(0); // 快速失败
        return rule;
    }

    /**
     * 初始化限流回调处理器
     */
    private void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (exchange, ex) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            result.put("data", null);
            result.put("timestamp", System.currentTimeMillis());

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
        log.info("Sentinel block handler initialized");
    }
}
