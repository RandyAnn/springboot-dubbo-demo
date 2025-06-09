package com.example.shared.config.cache;

import com.example.shared.cache.AsyncTwoLevelCache;
import com.example.shared.config.properties.CacheProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存配置类，提供Caffeine本地缓存和Redis二级缓存
 * 统一管理缓存键和过期时间，提供通用的缓存操作方法
 */
@Configuration
@EnableCaching
public class SharedCacheConfig {

    /**
     * 缓存系统配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "app.cache")
    public CacheProperties cacheProperties() {
        return new CacheProperties();
    }

    /**
     * 配置本地缓存管理器 (Caffeine)
     * 使用动态缓存创建，支持任意缓存名称
     */
    @Bean
    public CacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        CaffeineCacheManager mgr = new CaffeineCacheManager();
        CacheProperties.Local config = cacheProperties.getLocal();
        mgr.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(config.getExpireAfterWrite())  // 使用配置的过期时间
                .maximumSize(config.getMaximumSize())            // 使用配置的最大条目数
                .recordStats());                                 // 启用统计
        return mgr;
    }

    /**
     * 配置Redis缓存管理器作为二级缓存
     * 使用动态缓存创建，支持任意缓存名称
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                         @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
                                         CacheProperties cacheProperties) {
        // 使用配置好的Redis专用ObjectMapper创建序列化器，支持Java 8日期时间类型和类型信息
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getRedis().getTtl())  // 使用配置的过期时间
                .computePrefixWith(name -> name + "::")  // cacheName::key 格式
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 配置异步缓存操作的线程池
     */
    @Bean
    public Executor cacheAsyncExecutor(CacheProperties cacheProperties) {
        CacheProperties.Async config = cacheProperties.getAsync();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()),
                r -> {
                    Thread t = new Thread(r, "cache-async");
                    t.setDaemon(true);
                    return t;
                });
        return executor;
    }

    /**
     * 配置二级异步缓存管理器
     * 读操作：先本地缓存，再远程缓存，回填本地
     * 写操作：立刻更新本地缓存，异步更新远程缓存
     */
    @Bean
    @Primary
    public CacheManager twoLevelAsyncCacheManager(
            @Qualifier("caffeineCacheManager") CacheManager localCacheManager,
            @Qualifier("redisCacheManager") CacheManager remoteCacheManager,
            Executor cacheAsyncExecutor) {

        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                Cache localCache = localCacheManager.getCache(name);
                Cache remoteCache = remoteCacheManager.getCache(name);
                if (localCache != null && remoteCache != null) {
                    return new AsyncTwoLevelCache(name, localCache, remoteCache, cacheAsyncExecutor);
                }
                // 兜底：只有本地或只有远程
                return localCache != null ? localCache : remoteCache;
            }

            @Override
            public Collection<String> getCacheNames() {
                // 两个管理器的缓存名合集
                Set<String> names = new LinkedHashSet<>();
                names.addAll(localCacheManager.getCacheNames());
                names.addAll(remoteCacheManager.getCacheNames());
                return names;
            }
        };
    }
}
