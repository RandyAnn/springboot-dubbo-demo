package com.example.common.event.redis;

import com.example.common.event.DomainEvent;
import com.example.common.event.DomainEventHandler;
import com.example.common.event.EventListenerContainer;
import com.example.common.event.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component // 或者在Config中@Bean声明
public class RedisEventListenerContainer implements EventListenerContainer, InitializingBean, DisposableBean, MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisEventListenerContainer.class);

    private final RedisMessageListenerContainer redisContainer;
    private final ObjectMapper redisObjectMapper; // 用于反序列化
    private final RedisTemplate<String, Object> redisTemplate; // 用于获取序列化器

    private final List<MessageHandler> handlers = new CopyOnWriteArrayList<>();

    // 与Publisher一致的channel，实际项目中可以配置
    private static final String DEFAULT_EVENT_CHANNEL = "domain-events";

    public RedisEventListenerContainer(RedisMessageListenerContainer redisContainer,
                                       @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
                                       RedisTemplate<String, Object> redisTemplate) {
        this.redisContainer = redisContainer;
        this.redisObjectMapper = redisObjectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void registerHandler(MessageHandler handler) {
        if (handler != null) {
            this.handlers.add(handler);
            log.info("Registered MessageHandler: {}", handler.getClass().getName());
        }
    }

    @Override
    public void start() {
        if (!redisContainer.isRunning()) {
            redisContainer.start();
            log.info("RedisMessageListenerContainer started.");
        }
    }

    @Override
    public void stop() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
            log.info("RedisMessageListenerContainer stopped.");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 使用 this 作为 MessageListener
        // RedisTemplate 已经配置了valueSerializer，我们用它来反序列化
        // MessageListenerAdapter 也可以用，但直接实现 MessageListener 更灵活处理原始 Message
        redisContainer.addMessageListener(this, new ChannelTopic(DEFAULT_EVENT_CHANNEL));
        log.info("Subscribed to Redis channel: {}", DEFAULT_EVENT_CHANNEL);
        start(); // 自动启动监听
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        byte[] channelBytes = message.getChannel();
        String channel = new String(channelBytes != null ? channelBytes : new byte[0]);
        log.debug("Received message from Redis channel '{}'", channel);

        try {
            // 获取 RedisTemplate 中配置的 valueSerializer 用于反序列化
            @SuppressWarnings("unchecked")
            RedisSerializer<DomainEvent> valueSerializer = (RedisSerializer<DomainEvent>) redisTemplate.getValueSerializer();
            if (valueSerializer == null) {
                log.error("RedisTemplate value serializer is not configured correctly for DomainEvent deserialization.");
                // 尝试使用 redisObjectMapper 作为备选，但这可能不完全符合 redisTemplate 的配置
                // DomainEvent event = redisObjectMapper.readValue(body, DomainEvent.class);
                // 此处应确保valueSerializer被正确配置并获取
                 return;
            }

            DomainEvent event = valueSerializer.deserialize(body);

            if (event != null) {
                log.info("Deserialized event of type '{}' with ID '{}' from channel '{}'",
                         event.getClass().getSimpleName(), event.getEventId(), channel);
                for (MessageHandler handler : handlers) {
                    try {
                        if (handler instanceof DomainEventHandler) {
                            @SuppressWarnings("rawtypes")
                            DomainEventHandler domainEventHandler = (DomainEventHandler) handler;
                            if (domainEventHandler.supports(event.getClass())) {
                                log.debug("Dispatching event {} to handler {}", event.getClass().getSimpleName(), handler.getClass().getName());
                                domainEventHandler.onMessage(event); // onMessage内部会调用handle
                            }
                        } else {
                            // 对于非 DomainEventHandler 的通用 MessageHandler，直接调用
                            log.debug("Dispatching event {} to generic handler {}", event.getClass().getSimpleName(), handler.getClass().getName());
                            handler.onMessage(event);
                        }
                    } catch (Exception e) {
                        log.error("Error processing event {} with handler {}: {}",
                                  event, handler.getClass().getName(), e.getMessage(), e);
                        // 单个处理器异常不应影响其他处理器
                    }
                }
            } else {
                log.warn("Deserialized event is null from channel '{}'. Message body (hex): {}",
                         channel, bytesToHex(body));
            }
        } catch (Exception e) {
            log.error("Error deserializing or processing message from Redis channel '{}': {}",
                      channel, e.getMessage(), e);
            log.error("Failed message body (hex): {}", bytesToHex(body));
        }
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}