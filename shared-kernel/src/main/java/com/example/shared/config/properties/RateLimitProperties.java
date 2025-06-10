package com.example.shared.config.properties;

import lombok.Data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel限流熔断配置属性类
 * 统一管理Sentinel限流熔断的所有配置项，包括QPS限流、并发线程数限流、熔断降级等参数的可配置管理
 */
@Data
public class RateLimitProperties {

    /**
     * 是否启用Sentinel限流熔断功能
     */
    private boolean enabled = true;

    /**
     * 全局QPS限制
     */
    private int qpsLimit = 100;

    /**
     * 全局并发线程数限制
     */
    private int threadLimit = 50;

    /**
     * 熔断器配置
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * 限流规则配置列表
     */
    private List<RuleConfig> rules = new ArrayList<>();

    /**
     * 降级配置
     */
    private Degradation degradation = new Degradation();

    /**
     * 系统保护配置
     */
    private SystemProtection systemProtection = new SystemProtection();

    /**
     * 熔断器配置
     */
    @Data
    public static class CircuitBreaker {
        /**
         * 熔断阈值（错误率）
         */
        private double threshold = 0.5;

        /**
         * 最小请求数（触发熔断的最小请求数量）
         */
        private int minRequestAmount = 5;

        /**
         * 统计时间窗口（秒）
         */
        private int statIntervalMs = 1000;

        /**
         * 熔断持续时间（毫秒）
         */
        private int timeWindow = 10000;

        /**
         * 慢调用比例阈值
         */
        private double slowCallRatio = 1.0;

        /**
         * 慢调用RT阈值（毫秒）
         */
        private int slowCallRt = 4900;
    }

    /**
     * 限流规则配置
     */
    @Data
    public static class RuleConfig {
        /**
         * 资源名称
         */
        private String resource;

        /**
         * 限流类型：QPS 或 THREAD
         */
        private String grade = "QPS";

        /**
         * 限流阈值
         */
        private double count = 100;

        /**
         * 流控效果：FAST_FAIL, WARM_UP, RATE_LIMITER
         */
        private String controlBehavior = "FAST_FAIL";

        /**
         * 预热时间（毫秒）
         */
        private int warmUpPeriodSec = 10;

        /**
         * 最大排队时间（毫秒）
         */
        private int maxQueueingTimeMs = 500;

        /**
         * 是否集群模式
         */
        private boolean clusterMode = false;
    }

    /**
     * 降级配置
     */
    @Data
    public static class Degradation {
        /**
         * 是否启用降级
         */
        private boolean enabled = true;

        /**
         * 降级策略：RT, EXCEPTION_RATIO, EXCEPTION_COUNT
         */
        private String grade = "RT";

        /**
         * 降级阈值
         */
        private double count = 4900;

        /**
         * 时间窗口（秒）
         */
        private int timeWindow = 10;

        /**
         * 最小请求数
         */
        private int minRequestAmount = 5;

        /**
         * 统计时长（毫秒）
         */
        private int statIntervalMs = 1000;

        /**
         * 慢调用比例阈值
         */
        private double slowRatioThreshold = 1.0;
    }

    /**
     * 系统保护配置
     */
    @Data
    public static class SystemProtection {
        /**
         * 是否启用系统保护
         */
        private boolean enabled = false;

        /**
         * 系统负载阈值
         */
        private double highestSystemLoad = -1;

        /**
         * CPU使用率阈值
         */
        private double highestCpuUsage = -1;

        /**
         * 平均RT阈值
         */
        private long avgRt = -1;

        /**
         * 最大线程数阈值
         */
        private long maxThread = -1;

        /**
         * 入口QPS阈值
         */
        private double qps = -1;
    }

    /**
     * 获取指定资源的限流规则
     */
    public RuleConfig getRuleByResource(String resource) {
        return rules.stream()
                .filter(rule -> resource.equals(rule.getResource()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加限流规则
     */
    public void addRule(RuleConfig rule) {
        if (rule != null && rule.getResource() != null) {
            rules.add(rule);
        }
    }

    /**
     * 移除指定资源的限流规则
     */
    public boolean removeRule(String resource) {
        return rules.removeIf(rule -> resource.equals(rule.getResource()));
    }
}
