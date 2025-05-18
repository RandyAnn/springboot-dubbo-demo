package com.example.common.event.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis缓存事件消息监听器
 * 负责接收Redis发布的缓存事件消息，并分发给注册的CacheEventListener
 */
public class RedisCacheEventMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheEventMessageListener.class);

    private final List<CacheEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final RedisSerializer<String> stringSerializer;

    public RedisCacheEventMessageListener(RedisSerializer<String> stringSerializer) {
        this.stringSerializer = stringSerializer;
        this.objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDateTime反序列化
        this.objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的反序列化情况
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * 添加缓存事件监听器
     *
     * @param listener 缓存事件监听器
     */
    public void addListener(CacheEventListener listener) {
        listeners.add(listener);
        logger.info("已注册缓存事件监听器: {}", listener.getClass().getName());
    }

    /**
     * 移除缓存事件监听器
     *
     * @param listener 缓存事件监听器
     */
    public void removeListener(CacheEventListener listener) {
        listeners.remove(listener);
        logger.info("已移除缓存事件监听器: {}", listener.getClass().getName());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 将消息内容转换为字符串
            String jsonMessage = stringSerializer.deserialize(message.getBody());
            if (jsonMessage == null) {
                logger.warn("接收到空的缓存事件消息");
                return;
            }

            // 处理可能的额外引号问题
            if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
                // 去除首尾引号并处理转义字符
                jsonMessage = jsonMessage.substring(1, jsonMessage.length() - 1).replace("\\\"", "\"");
            }

            try {
                // 将JSON字符串反序列化为CacheEvent对象
                CacheEvent event = objectMapper.readValue(jsonMessage, CacheEvent.class);

                // 分发事件给所有支持该事件的监听器
                for (CacheEventListener listener : listeners) {
                    if (listener.supports(event)) {
                        try {
                            listener.onEvent(event);
                        } catch (Exception e) {
                            logger.error("监听器处理缓存事件失败: {}", listener.getClass().getName(), e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("反序列化JSON消息失败: {}", jsonMessage, e);
            }
        } catch (Exception e) {
            logger.error("处理缓存事件消息失败", e);
        }
    }
}
