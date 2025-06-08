package com.example.shared.event.redis;

import com.example.shared.event.DomainEvent;
import com.example.shared.event.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisEventPublisher.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper; // 用于确定channel名称或序列化

    // 假设事件都发布到同一个channel，或者根据事件类型动态确定channel
    // 为了简单起见，这里使用固定的channel名，实际项目中可以配置
    private static final String DEFAULT_EVENT_CHANNEL = "domain-events";

    public RedisEventPublisher(RedisTemplate<String, Object> redisTemplate,
                               @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisObjectMapper = redisObjectMapper; // 注意：redisTemplate本身已经配置了序列化器
                                               // 此处注入ObjectMapper主要是为了获取事件类型作为channel的一部分，或自定义更复杂的逻辑
                                               // 如果只是简单发布，redisTemplate内部的序列化器已足够。
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish a null event.");
            return;
        }
        try {
            // 可以根据事件类型或其他逻辑选择不同的channel
            // String channel = DEFAULT_EVENT_CHANNEL + ":" + event.getClass().getSimpleName();
            String channel = DEFAULT_EVENT_CHANNEL;
            log.info("Publishing event of type '{}' with ID '{}' to Redis channel '{}'",
                     event.getClass().getSimpleName(), event.getEventId(), channel);

            // RedisTemplate 已经配置了序列化器 (GenericJackson2JsonRedisSerializer with redisObjectMapper)
            // 所以可以直接传递 event 对象，它会被正确序列化。
            redisTemplate.convertAndSend(channel, event);
            log.debug("Successfully published event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing event {} to Redis: {}", event, e.getMessage(), e);
            // 根据需要处理异常，例如重试或记录到死信队列
        }
    }
}
