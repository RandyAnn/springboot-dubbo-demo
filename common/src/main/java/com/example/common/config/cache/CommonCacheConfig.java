package com.example.common.config.cache;

import com.example.common.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Bean
    @ConfigurationProperties(prefix = "cache")
    public CacheProperties cacheProperties() {
        return new CacheProperties();
    }

    /**
     * 配置本地缓存管理器 (Caffeine)
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<>();

        // 默认缓存配置
        Map<String, CacheSpec> defaultCaches = new HashMap<>();
        defaultCaches.put(USER_INFO_CACHE, new CacheSpec(1000, 10));
        defaultCaches.put(USER_NUTRITION_GOAL_CACHE, new CacheSpec(500, 24 * 60)); // 用户营养目标缓存，默认24小时
        defaultCaches.put(FILE_URL_CACHE, new CacheSpec(2000, 25));  // 预签名URL缓存，默认25分钟
        defaultCaches.put(FOOD_INFO_CACHE, new CacheSpec(500, 30));
        defaultCaches.put(FOOD_CATEGORY_CACHE, new CacheSpec(100, 60)); // 分类缓存，默认60分钟
        defaultCaches.put(NUTRITION_STATS_CACHE, new CacheSpec(200, 15));
        defaultCaches.put(DIET_RECORD_CACHE, new CacheSpec(500, 20)); // 饮食记录缓存，默认20分钟
        defaultCaches.put(DIET_STATS_CACHE, new CacheSpec(300, 15)); // 饮食统计缓存，默认15分钟
        defaultCaches.put(HEALTH_REPORT_CACHE, new CacheSpec(200, 24 * 60)); // 健康报告缓存，默认24小时

        // 合并自定义配置和默认配置
        Map<String, CacheSpec> cacheSpecs = cacheProperties.getSpecs() != null
                ? cacheProperties.getSpecs() : new HashMap<>();

        // 使用默认配置作为基础，然后覆盖自定义配置
        for (Map.Entry<String, CacheSpec> entry : defaultCaches.entrySet()) {
            String cacheName = entry.getKey();
            CacheSpec spec = cacheSpecs.getOrDefault(cacheName, entry.getValue());

            caches.add(new CaffeineCache(cacheName,
                    Caffeine.newBuilder()
                            .maximumSize(spec.getMaxSize())
                            .expireAfterWrite(spec.getTtlMinutes(), TimeUnit.MINUTES)
                            .recordStats()
                            .build()));
        }

        // 添加自定义缓存（不在默认列表中的）
        for (Map.Entry<String, CacheSpec> entry : cacheSpecs.entrySet()) {
            if (!defaultCaches.containsKey(entry.getKey())) {
                CacheSpec spec = entry.getValue();
                caches.add(new CaffeineCache(entry.getKey(),
                        Caffeine.newBuilder()
                                .maximumSize(spec.getMaxSize())
                                .expireAfterWrite(spec.getTtlMinutes(), TimeUnit.MINUTES)
                                .recordStats()
                                .build()));
            }
        }

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * 配置支持Java 8日期时间类型的ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDate、LocalDateTime等序列化
        objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的序列化情况
        objectMapper.findAndRegisterModules();
        // 配置日期时间格式，不使用时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    /**
     * 配置Redis缓存管理器作为二级缓存
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, CacheProperties cacheProperties, ObjectMapper objectMapper) {
        // 使用配置好的ObjectMapper创建序列化器，支持Java 8日期时间类型
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(cacheProperties.getRedisTtlMinutes()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer)
                );

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        // 为不同的缓存配置不同的过期时间
        configMap.put(USER_INFO_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(USER_INFO_CACHE))));
        configMap.put(USER_NUTRITION_GOAL_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(USER_NUTRITION_GOAL_CACHE))));
        configMap.put(FILE_URL_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(FILE_URL_CACHE))));
        configMap.put(FOOD_INFO_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(FOOD_INFO_CACHE))));
        configMap.put(FOOD_CATEGORY_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(FOOD_CATEGORY_CACHE))));
        configMap.put(NUTRITION_STATS_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(NUTRITION_STATS_CACHE))));
        configMap.put(DIET_RECORD_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(DIET_RECORD_CACHE))));
        configMap.put(DIET_STATS_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(DIET_STATS_CACHE))));
        configMap.put(HEALTH_REPORT_CACHE, config.entryTtl(Duration.ofMinutes(defaultCacheTtl(HEALTH_REPORT_CACHE))));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(configMap)
                .build();
    }

    /**
     * 配置统一的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        // 传入配置好的ObjectMapper，确保能够处理Java 8日期时间类型
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建缓存服务Bean，提供统一的缓存操作接口
     */
    @Bean
    public CacheService cacheService(RedisTemplate<String, Object> redisTemplate,
                                     CacheManager cacheManager,
                                     ObjectMapper objectMapper) {
        return new CacheService(redisTemplate, cacheManager, objectMapper);
    }

    /**
     * 获取默认的缓存过期时间
     */
    private long defaultCacheTtl(String cacheName) {
        switch (cacheName) {
            case USER_INFO_CACHE: return 10;
            case USER_NUTRITION_GOAL_CACHE: return 24 * 60; // 24小时
            case FILE_URL_CACHE: return 25;
            case FOOD_INFO_CACHE: return 30;
            case FOOD_CATEGORY_CACHE: return 60;
            case NUTRITION_STATS_CACHE: return 15;
            case DIET_RECORD_CACHE: return 20;
            case DIET_STATS_CACHE: return 15;
            case HEALTH_REPORT_CACHE: return 24 * 60; // 24小时
            default: return 30;
        }
    }

    /**
     * 缓存配置属性类
     */
    public static class CacheProperties {
        private Map<String, CacheSpec> specs;
        private long redisTtlMinutes = 30; // 默认Redis缓存过期时间为30分钟

        public Map<String, CacheSpec> getSpecs() {
            return specs;
        }

        public void setSpecs(Map<String, CacheSpec> specs) {
            this.specs = specs;
        }

        public long getRedisTtlMinutes() {
            return redisTtlMinutes;
        }

        public void setRedisTtlMinutes(long redisTtlMinutes) {
            this.redisTtlMinutes = redisTtlMinutes;
        }
    }

    /**
     * 缓存规格配置
     */
    public static class CacheSpec {
        private long maxSize = 1000; // 默认最大缓存条目数
        private long ttlMinutes = 10; // 默认过期时间（分钟）

        public CacheSpec() {
        }

        public CacheSpec(long maxSize, long ttlMinutes) {
            this.maxSize = maxSize;
            this.ttlMinutes = ttlMinutes;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
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