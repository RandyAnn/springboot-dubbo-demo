package com.example.common.config.cache;

import com.example.common.cache.AsyncTwoLevelCache;
import com.example.common.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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
public class CommonCacheConfig {
    // 缓存名称常量，可供各模块使用
    public static final String USER_INFO_CACHE = "userInfoCache";
    public static final String USER_NUTRITION_GOAL_CACHE = "userNutritionGoalCache";
    public static final String FILE_URL_CACHE = "fileUrlCache";
    public static final String FOOD_INFO_CACHE = "foodInfoCache";
    public static final String FOOD_CATEGORY_CACHE = "foodCategoryCache";
    public static final String NUTRITION_STATS_CACHE = "nutritionStatsCache";
    public static final String DIET_RECORD_CACHE = "dietRecordCache";
    public static final String DIET_STATS_CACHE = "dietStatsCache";
    public static final String HEALTH_REPORT_CACHE = "healthReportCache";

    // Redis缓存键前缀
    public static final String REDIS_USER_INFO_PREFIX = "user:info:";
    public static final String REDIS_USER_NUTRITION_GOAL_PREFIX = "user:nutrition:goal:";
    public static final String REDIS_FILE_URL_PREFIX = "file:url:";
    public static final String REDIS_FOOD_INFO_PREFIX = "food:info:";
    public static final String REDIS_FOOD_CATEGORY_PREFIX = "food:category:";
    public static final String REDIS_NUTRITION_STATS_PREFIX = "nutrition:stats:";
    public static final String REDIS_DIET_RECORD_PREFIX = "diet:record:";
    public static final String REDIS_DIET_STATS_PREFIX = "diet:stats:";
    public static final String REDIS_HEALTH_REPORT_PREFIX = "health:report:";



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
     * 配置支持Java 8日期时间类型的ObjectMapper（用于HTTP响应，不包含类型信息）
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDate、LocalDateTime等序列化
        objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的序列化情况
        objectMapper.findAndRegisterModules();
        // 配置日期时间格式，不使用时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // HTTP响应不需要类型信息
        return objectMapper;
    }

    /**
     * 配置专门用于Redis缓存的ObjectMapper（包含类型信息）
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDate、LocalDateTime等序列化
        objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的序列化情况
        objectMapper.findAndRegisterModules();
        // 配置日期时间格式，不使用时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 启用默认类型信息，解决Redis缓存反序列化问题
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
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
     * 配置统一的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        // 传入配置好的Redis专用ObjectMapper，确保能够处理Java 8日期时间类型和类型信息
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
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

    /**
     * 创建缓存服务Bean，提供统一的缓存操作接口
     */
    @Bean
    public CacheService cacheService(RedisTemplate<String, Object> redisTemplate,
                                     @Qualifier("caffeineCacheManager") CacheManager cacheManager,
                                     @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return new CacheService(redisTemplate, cacheManager, redisObjectMapper);
    }



    /**
     * 获取Redis缓存键前缀
     */
    public static String getRedisCachePrefix(String cacheName) {
        switch (cacheName) {
            case USER_INFO_CACHE: return REDIS_USER_INFO_PREFIX;
            case USER_NUTRITION_GOAL_CACHE: return REDIS_USER_NUTRITION_GOAL_PREFIX;
            case FILE_URL_CACHE: return REDIS_FILE_URL_PREFIX;
            case FOOD_INFO_CACHE: return REDIS_FOOD_INFO_PREFIX;
            case FOOD_CATEGORY_CACHE: return REDIS_FOOD_CATEGORY_PREFIX;
            case NUTRITION_STATS_CACHE: return REDIS_NUTRITION_STATS_PREFIX;
            case DIET_RECORD_CACHE: return REDIS_DIET_RECORD_PREFIX;
            case DIET_STATS_CACHE: return REDIS_DIET_STATS_PREFIX;
            case HEALTH_REPORT_CACHE: return REDIS_HEALTH_REPORT_PREFIX;
            default: return "cache:";
        }
    }

    /**
     * 构建Redis缓存键
     */
    public static String buildRedisCacheKey(String cacheName, Object key) {
        return getRedisCachePrefix(cacheName) + key.toString();
    }
}