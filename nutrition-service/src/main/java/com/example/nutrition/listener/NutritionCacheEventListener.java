package com.example.nutrition.listener;

import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.event.cache.AbstractCacheEventListener;
import com.example.common.event.cache.CacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 营养服务缓存事件监听器
 * 负责处理与营养服务相关的缓存事件
 */
@Component
public class NutritionCacheEventListener extends AbstractCacheEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NutritionCacheEventListener.class);

    private final CacheService cacheService;

    @Autowired
    public NutritionCacheEventListener(CacheService cacheService) {
        // 指定关注的缓存名称
        super(
            CommonCacheConfig.DIET_RECORD_CACHE,
            CommonCacheConfig.NUTRITION_STATS_CACHE,
            CommonCacheConfig.DIET_STATS_CACHE
        );
        this.cacheService = cacheService;
    }

    @Override
    protected void handleEvictEvent(CacheEvent event) {
        logger.info("处理缓存清除事件: {}", event);

        // 如果是饮食记录缓存被清除，需要清除相关的营养统计缓存
        if (CommonCacheConfig.DIET_RECORD_CACHE.equals(event.getCacheName())) {
            // 从缓存键中提取用户ID
            // 假设缓存键格式为 "user:userId" 或 "detail:userId:recordId"
            String cacheKey = event.getCacheKey();
            if (cacheKey != null) {
                String userId = null;
                if (cacheKey.startsWith("user:")) {
                    userId = cacheKey.substring(5);
                } else if (cacheKey.startsWith("detail:")) {
                    String[] parts = cacheKey.split(":");
                    if (parts.length > 1) {
                        userId = parts[1];
                    }
                }

                if (userId != null) {
                    // 清除该用户的所有营养统计缓存（使用模式匹配清除所有包含该用户ID的缓存键）
                    String userNutritionCachePattern = "daily:" + userId + "*";
                    cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userNutritionCachePattern);

                    // 清除该用户的营养建议缓存
                    String userAdviceCachePattern = "advice:" + userId + "*";
                    cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userAdviceCachePattern);

                    String userTrendCachePattern = "trend:" + userId + "*";
                    cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userTrendCachePattern);

                    // 清除该用户的营养详情缓存
                    String userDetailsCachePattern = "details:" + userId + "*";
                    cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userDetailsCachePattern);

                    // 清除该用户的健康报告缓存
                    String userHealthReportCachePattern = "report:" + userId + "*";
                    cacheService.evictByPattern(CommonCacheConfig.HEALTH_REPORT_CACHE, userHealthReportCachePattern);
                }
            }

            logger.info("饮食记录缓存被清除，已清除相关的营养统计数据缓存");
        }
    }

    @Override
    protected void handleClearEvent(CacheEvent event) {
        logger.info("处理缓存清空事件: {}", event);

        // 如果是饮食记录缓存被清空，需要清除所有营养统计缓存和健康报告缓存
        if (CommonCacheConfig.DIET_RECORD_CACHE.equals(event.getCacheName())) {
            // 清除所有营养统计缓存
            cacheService.clear(CommonCacheConfig.NUTRITION_STATS_CACHE);
            // 清除所有健康报告缓存
            cacheService.clear(CommonCacheConfig.HEALTH_REPORT_CACHE);
        }
    }

    @Override
    protected void handleUpdateEvent(CacheEvent event) {
        logger.info("处理缓存更新事件: {}", event);

        // 对于更新事件，需要清除相关缓存
        handleEvictEvent(event);
    }

    @Override
    protected void handlePutEvent(CacheEvent event) {
        logger.info("处理缓存添加事件: {}", event);

        // 但如果有依赖关系，需要清除相关缓存
        if (CommonCacheConfig.DIET_RECORD_CACHE.equals(event.getCacheName()) && event.getCacheKey() != null) {
            // 从缓存键中提取用户ID
            String userId = null;
            String cacheKey = event.getCacheKey();

            if (cacheKey.startsWith("user:")) {
                userId = cacheKey.substring(5);
            } else if (cacheKey.contains(":")) {
                // 尝试从其他格式的键中提取用户ID
                String[] parts = cacheKey.split(":");
                if (parts.length > 1) {
                    // 对于大多数格式，用户ID通常是第一个或第二个部分
                    userId = parts[0].equals("user") ? parts[1] : parts[0];
                }
            }

            if (userId != null) {
                // 清除该用户的所有营养统计缓存（使用模式匹配清除所有包含该用户ID的缓存键）
                String userNutritionCachePattern = "daily:" + userId + "*";
                cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userNutritionCachePattern);

                // 清除该用户的营养建议缓存
                String userAdviceCachePattern = "advice:" + userId + "*";
                cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userAdviceCachePattern);

                // 清除该用户的营养详情缓存
                String userDetailsCachePattern = "details:" + userId + "*";
                cacheService.evictByPattern(CommonCacheConfig.NUTRITION_STATS_CACHE, userDetailsCachePattern);

                // 清除该用户的健康报告缓存
                String userHealthReportCachePattern = "report:" + userId + "*";
                cacheService.evictByPattern(CommonCacheConfig.HEALTH_REPORT_CACHE, userHealthReportCachePattern);
            }
        }
    }
}
