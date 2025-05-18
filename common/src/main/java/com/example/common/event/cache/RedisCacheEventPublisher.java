package com.example.common.event.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis实现的缓存事件发布器
 * 通过Redis的发布/订阅机制发布缓存事件
 */
public class RedisCacheEventPublisher implements CacheEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheEventPublisher.class);

    /**
     * Redis缓存事件通道名称
     */
    public static final String CACHE_EVENT_CHANNEL = "cache:events";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheEventPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDateTime序列化
        this.objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的序列化情况
        this.objectMapper.findAndRegisterModules();
        // 配置日期时间格式
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void publishEvent(CacheEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CACHE_EVENT_CHANNEL, eventJson);
        } catch (JsonProcessingException e) {
            logger.error("序列化缓存事件失败", e);
        } catch (Exception e) {
            logger.error("发布缓存事件失败", e);
        }
    }
}
