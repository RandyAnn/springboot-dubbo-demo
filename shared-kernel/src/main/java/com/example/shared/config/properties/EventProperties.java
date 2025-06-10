package com.example.shared.config.properties;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * 事件系统配置属性类
 * 统一管理事件系统的所有配置项，包括provider、channel、consumer、线程池、Kafka等配置
 */
@Data
public class EventProperties {

    /**
     * 事件提供者类型，可选值: redis, kafka
     */
    private String provider = "redis";

    /**
     * 事件channel/topic名称，Redis和Kafka统一使用此配置
     */
    private String channel = "domain-events";

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * Kafka相关配置
     */
    private Kafka kafka = new Kafka();

    /**
     * 消费者配置
     */
    @Data
    public static class Consumer {
        /**
         * 是否启用事件消费者
         */
        private boolean enabled = false;
    }

    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPool {
        /**
         * 核心线程数
         */
        private int coreSize = 5;

        /**
         * 最大线程数
         */
        private int maxSize = 10;

        /**
         * 队列容量
         */
        private int queueCapacity = 1000;

        /**
         * 线程名称前缀
         */
        private String namePrefix = "event-listener-";
    }

    /**
     * Kafka相关配置
     */
    @Data
    public static class Kafka {
        /**
         * Kafka监听器并发数
         */
        private int concurrency = 3;

        /**
         * JsonDeserializer信任的包列表
         */
        private List<String> trustedPackages = Arrays.asList(
            "com.example.shared.event",
            "com.example.diet.event",
            "com.example.nutrition.event"
        );
    }
}
