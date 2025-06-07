package com.example.shared.config.cache;

import com.example.shared.cache.AsyncTwoLevelCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
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
     * 配置本地缓存管理器 (Caffeine)
     * 使用动态缓存创建，支持任意缓存名称
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager();
        mgr.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)  // 统一的过期时间
                .maximumSize(10_000)                     // 统一的最大条目数
                .recordStats());                         // 启用统计
        return mgr;
    }

    /**
     * 配置Redis缓存管理器作为二级缓存
     * 使用动态缓存创建，支持任意缓存名称
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                         @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        // 使用配置好的Redis专用ObjectMapper创建序列化器，支持Java 8日期时间类型和类型信息
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 统一的过期时间
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
    public Executor cacheAsyncExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
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
