package com.example.common.config.event;

import com.example.common.event.DomainEvent;
import com.example.common.event.EventListenerContainer;
import com.example.common.event.EventPublisher;
import com.example.common.event.MessageHandler;
import com.example.common.event.cache.CacheClearEvent;
import com.example.common.event.cache.CacheEvictEvent;
import com.example.common.event.cache.CachePutEvent;
import com.example.common.event.cache.CacheUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件系统自动配置类
 * 提供基于Redis的事件发布和订阅默认实现
 */
@Configuration
public class EventAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EventAutoConfiguration.class);

    /**
     * 事件主题模式，订阅所有以Event结尾的主题
     */
    private static final String EVENT_TOPIC_PATTERN = "*Event";

    /**
     * 创建基于Redis的事件发布器
     * 如果Spring容器中没有其他EventPublisher实现，则使用Redis Pub/Sub
     *
     * @param stringRedisTemplate Redis字符串模板
     * @param objectMapper JSON序列化器
     * @return Redis事件发布器
     */
    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher redisEventPublisher(StringRedisTemplate stringRedisTemplate,
                                              ObjectMapper objectMapper) {
        logger.info("创建Redis事件发布器");
        return event -> {
            try {
                String topic = event.getEventType();
                String body = objectMapper.writeValueAsString(event);
                stringRedisTemplate.convertAndSend(topic, body);
                logger.debug("发布事件到主题 {}: {}", topic, event.getEventId());
            } catch (Exception e) {
                logger.error("发布事件失败: {}", event, e);
                throw new RuntimeException("发布事件失败", e);
            }
        };
    }

    /**
     * 创建基于Redis的事件监听容器
     * 如果Spring容器中没有其他EventListenerContainer实现，则使用Redis Pub/Sub
     *
     * @param connectionFactory Redis连接工厂
     * @param objectMapper JSON序列化器
     * @param messageHandlers 消息处理器列表
     * @return Redis事件监听容器
     */
    @Bean
    @ConditionalOnMissingBean(EventListenerContainer.class)
    public EventListenerContainer redisListenerContainer(RedisConnectionFactory connectionFactory,
                                                         ObjectMapper objectMapper,
                                                         List<MessageHandler> messageHandlers) {
        logger.info("创建Redis事件监听容器");

        // 使用线程安全的列表存储处理器
        List<MessageHandler> handlers = new CopyOnWriteArrayList<>(messageHandlers);

        // 创建Redis消息监听容器
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 添加消息监听器，订阅所有以Event结尾的主题
        container.addMessageListener((message, pattern) -> {
            try {
                String body = new String(message.getBody());
                String channel = new String(message.getChannel());
                logger.debug("接收到事件消息，频道: {}, 内容: {}", channel, body);

                // 根据频道名称确定事件类型并反序列化
                DomainEvent event = deserializeEvent(objectMapper, channel, body);
                if (event == null) {
                    logger.warn("无法反序列化事件，频道: {}", channel);
                    return;
                }

                // 分发给所有处理器
                for (MessageHandler handler : handlers) {
                    try {
                        handler.onMessage(event);
                    } catch (Exception e) {
                        logger.error("处理器 {} 处理事件失败: {}",
                                   handler.getClass().getSimpleName(), event, e);
                    }
                }
            } catch (Exception e) {
                logger.error("处理Redis消息失败", e);
            }
        }, new PatternTopic(EVENT_TOPIC_PATTERN));

        return new EventListenerContainer() {
            @Override
            public void registerHandler(MessageHandler handler) {
                handlers.add(handler);
                logger.info("注册消息处理器: {}", handler.getClass().getSimpleName());
            }

            @Override
            public void start() {
                container.start();
                logger.info("事件监听容器已启动，订阅主题模式: {}", EVENT_TOPIC_PATTERN);
            }

            @Override
            public void stop() {
                container.stop();
                logger.info("事件监听容器已停止");
            }
        };
    }

    /**
     * 应用启动时自动启动事件监听容器
     *
     * @param container 事件监听容器
     * @return 应用启动器
     */
    @Bean
    public ApplicationRunner eventContainerStarter(EventListenerContainer container) {
        return args -> {
            logger.info("启动事件监听容器...");
            container.start();
        };
    }

    /**
     * 根据频道名称反序列化事件
     *
     * @param objectMapper JSON序列化器
     * @param channel 频道名称
     * @param body 消息体
     * @return 反序列化后的事件，如果失败则返回null
     */
    private static DomainEvent deserializeEvent(ObjectMapper objectMapper, String channel, String body) {
        try {
            // 根据频道名称确定事件类型
            Class<? extends DomainEvent> eventClass = getEventClassByChannel(channel);
            if (eventClass != null) {
                return objectMapper.readValue(body, eventClass);
            }

            // 如果无法确定具体类型，尝试反序列化为基类
            return objectMapper.readValue(body, DomainEvent.class);
        } catch (Exception e) {
            logger.error("反序列化事件失败，频道: {}, 内容: {}", channel, body, e);
            return null;
        }
    }

    /**
     * 根据频道名称获取事件类型
     *
     * @param channel 频道名称
     * @return 事件类型，如果无法确定则返回null
     */
    private static Class<? extends DomainEvent> getEventClassByChannel(String channel) {
        switch (channel) {
            case "CacheEvictEvent":
                return CacheEvictEvent.class;
            case "CacheClearEvent":
                return CacheClearEvent.class;
            case "CacheUpdateEvent":
                return CacheUpdateEvent.class;
            case "CachePutEvent":
                return CachePutEvent.class;
            default:
                return null;
        }
    }
}
