package com.example.gateway.config;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.example.shared.config.properties.RateLimitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel限流熔断配置类
 * 配置限流规则、熔断规则和异常处理
 */
@Configuration
public class SentinelConfig {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    /**
     * 初始化Sentinel规则
     */
    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
    }

    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        if (!rateLimitProperties.isEnabled()) {
            return;
        }

        List<FlowRule> rules = new ArrayList<>();

        // 从配置中读取限流规则
        for (RateLimitProperties.RuleConfig ruleConfig : rateLimitProperties.getRules()) {
            FlowRule flowRule = new FlowRule();
            flowRule.setResource(ruleConfig.getResource());
            flowRule.setGrade("QPS".equals(ruleConfig.getGrade()) ? 1 : 0);
            flowRule.setCount(ruleConfig.getCount());
            flowRule.setStrategy(0); // 直接限流

            // 设置流控效果
            switch (ruleConfig.getControlBehavior()) {
                case "WARM_UP":
                    flowRule.setControlBehavior(1);
                    flowRule.setWarmUpPeriodSec(ruleConfig.getWarmUpPeriodSec());
                    break;
                case "RATE_LIMITER":
                    flowRule.setControlBehavior(2);
                    flowRule.setMaxQueueingTimeMs(ruleConfig.getMaxQueueingTimeMs());
                    break;
                default:
                    flowRule.setControlBehavior(0); // FAST_FAIL
            }

            flowRule.setClusterMode(ruleConfig.isClusterMode());
            rules.add(flowRule);
        }

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 初始化熔断规则
     */
    private void initDegradeRules() {
        if (!rateLimitProperties.isEnabled() || !rateLimitProperties.getDegradation().isEnabled()) {
            return;
        }

        List<DegradeRule> rules = new ArrayList<>();

        // 使用配置中的熔断规则
        RateLimitProperties.Degradation degradationConfig = rateLimitProperties.getDegradation();
        RateLimitProperties.CircuitBreaker circuitBreakerConfig = rateLimitProperties.getCircuitBreaker();

        // 为每个配置的限流规则创建对应的熔断规则
        for (RateLimitProperties.RuleConfig ruleConfig : rateLimitProperties.getRules()) {
            DegradeRule degradeRule = new DegradeRule();
            degradeRule.setResource(ruleConfig.getResource());

            // 设置熔断策略
            switch (degradationConfig.getGrade()) {
                case "EXCEPTION_RATIO":
                    degradeRule.setGrade(1);
                    degradeRule.setCount(circuitBreakerConfig.getThreshold());
                    break;
                case "EXCEPTION_COUNT":
                    degradeRule.setGrade(2);
                    degradeRule.setCount(degradationConfig.getCount());
                    break;
                default: // RT
                    degradeRule.setGrade(0);
                    degradeRule.setCount(degradationConfig.getCount());
                    degradeRule.setSlowRatioThreshold(degradationConfig.getSlowRatioThreshold());
            }

            degradeRule.setTimeWindow(degradationConfig.getTimeWindow());
            degradeRule.setMinRequestAmount(degradationConfig.getMinRequestAmount());
            degradeRule.setStatIntervalMs(degradationConfig.getStatIntervalMs());

            rules.add(degradeRule);
        }

        DegradeRuleManager.loadRules(rules);
    }
}
