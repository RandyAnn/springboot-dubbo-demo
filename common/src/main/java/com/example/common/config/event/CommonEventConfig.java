package com.example.common.config.event;

import com.example.common.event.EventListenerContainer;
import com.example.common.event.EventPublisher;
import com.example.common.event.MessageHandler;
import com.example.common.event.redis.RedisEventListenerContainer;
import com.example.common.event.redis.RedisEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;

@Configuration
public class CommonEventConfig {

    /**
     * Redis Pub/Sub 事件发布器
     */
    @Bean
    public EventPublisher redisEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return new RedisEventPublisher(redisTemplate, redisObjectMapper);
    }

    /**
     * 配置 RedisMessageListenerContainer，用于监听Redis频道消息
     *
     * @param connectionFactory Redis连接工厂
     * @param messageListenerExecutor 异步执行消息监听器的线程池
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("messageListenerExecutor") Executor messageListenerExecutor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 设置用于执行消息侦听器任务的TaskExecutor。默认为SimpleAsyncTaskExecutor。
        // 使用自定义线程池以更好地控制资源和命名线程
        container.setTaskExecutor(messageListenerExecutor);
        // container.setSubscriptionExecutor() // 也可以为订阅操作设置单独的执行器
        return container;
    }

    /**
     * 为 RedisMessageListenerContainer 提供一个专用的线程池。
     * 这个线程池用于异步处理接收到的消息。
     */
    @Bean
    public Executor messageListenerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 根据预期负载调整
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("redis-event-listener-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            // log or handle rejection
            System.err.println("Task rejected from redis-event-listener: " + r.toString());
        });
        executor.initialize();
        return executor;
    }

    /**
     * Redis Pub/Sub 事件监听容器
     * 它会自动注册所有 MessageHandler 类型的Bean。
     */
    @Bean
    public EventListenerContainer redisEventListenerContainer(
            RedisMessageListenerContainer redisContainer, // Spring Data Redis 提供的容器
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
            RedisTemplate<String, Object> redisTemplate,
            List<MessageHandler> messageHandlers // 自动注入所有MessageHandler类型的Bean
            ) {
        RedisEventListenerContainer container = new RedisEventListenerContainer(redisContainer, redisObjectMapper, redisTemplate);
        if (messageHandlers != null) {
            messageHandlers.forEach(container::registerHandler);
        }
        // RedisEventListenerContainer 的 afterPropertiesSet 将自动启动监听
        return container;
    }
} 