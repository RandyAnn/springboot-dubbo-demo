package com.example.common.config.event;

import com.example.common.event.cache.CacheEventListener;
import com.example.common.event.cache.CacheEventPublisher;
import com.example.common.event.cache.RedisCacheEventMessageListener;
import com.example.common.event.cache.RedisCacheEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存事件配置类
 * 负责注册缓存事件相关的组件
 */
@Configuration
public class CacheEventConfig {

    /**
     * 创建Redis缓存事件发布器
     *
     * @param redisTemplate Redis模板
     * @param redisObjectMapper Redis专用ObjectMapper
     * @return 缓存事件发布器
     */
    @Bean
    public CacheEventPublisher cacheEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                                   @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return new RedisCacheEventPublisher(redisTemplate, redisObjectMapper);
    }

    /**
     * 创建Redis缓存事件消息监听器
     *
     * @param listenersProvider 缓存事件监听器提供者
     * @param redisObjectMapper Redis专用ObjectMapper
     * @return 缓存事件消息监听器
     */
    @Bean
    public RedisCacheEventMessageListener redisCacheEventMessageListener(
            ObjectProvider<CacheEventListener> listenersProvider,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisCacheEventMessageListener listener = new RedisCacheEventMessageListener(new StringRedisSerializer(), redisObjectMapper);

        // 注册所有可用的监听器
        listenersProvider.forEach(listener::addListener);

        return listener;
    }

    /**
     * 创建Redis消息监听容器
     *
     * @param connectionFactory Redis连接工厂
     * @param messageListener 缓存事件消息监听器
     * @return Redis消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisCacheEventMessageListener messageListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListener,
                new ChannelTopic(RedisCacheEventPublisher.CACHE_EVENT_CHANNEL));
        return container;
    }
}
