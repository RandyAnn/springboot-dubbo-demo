package com.example.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存系统配置属性类
 * 统一管理缓存系统的所有配置项，包括本地缓存、Redis缓存、异步线程池等配置
 */
@Data
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    /**
     * 本地缓存配置（Caffeine）
     */
    private Local local = new Local();

    /**
     * Redis缓存配置
     */
    private Redis redis = new Redis();

    /**
     * 异步操作线程池配置
     */
    private Async async = new Async();

    /**
     * 本地缓存配置
     */
    @Data
    public static class Local {
        /**
         * 缓存写入后过期时间
         */
        private Duration expireAfterWrite = Duration.ofMinutes(10);

        /**
         * 缓存最大条目数
         */
        private long maximumSize = 10000;
    }

    /**
     * Redis缓存配置
     */
    @Data
    public static class Redis {
        /**
         * 缓存过期时间（TTL）
         */
        private Duration ttl = Duration.ofMinutes(30);
    }

    /**
     * 异步操作线程池配置
     */
    @Data
    public static class Async {
        /**
         * 核心线程数
         */
        private int corePoolSize = 5;

        /**
         * 最大线程数
         */
        private int maxPoolSize = 10;

        /**
         * 线程空闲时间（秒）
         */
        private long keepAliveSeconds = 60;

        /**
         * 队列容量
         */
        private int queueCapacity = 1000;
    }
}
