package com.example.common.event.cache;

/**
 * 缓存事件发布器接口
 * 负责对外提供缓存事件的发布能力
 */
public interface CacheEventPublisher {
    
    /**
     * 发布缓存事件
     * 
     * @param event 缓存事件
     */
    void publishEvent(CacheEvent event);
    
    /**
     * 发布清除特定缓存键的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     */
    default void publishEvictEvent(String cacheName, String cacheKey, String sourceService) {
        publishEvent(CacheEvent.evictEvent(cacheName, cacheKey, sourceService));
    }
    
    /**
     * 发布清除整个缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param sourceService 源服务名称
     */
    default void publishClearEvent(String cacheName, String sourceService) {
        publishEvent(CacheEvent.clearEvent(cacheName, sourceService));
    }
    
    /**
     * 发布更新缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     */
    default void publishUpdateEvent(String cacheName, String cacheKey, String sourceService) {
        publishEvent(CacheEvent.updateEvent(cacheName, cacheKey, sourceService));
    }
    
    /**
     * 发布添加缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     */
    default void publishPutEvent(String cacheName, String cacheKey, String sourceService) {
        publishEvent(CacheEvent.putEvent(cacheName, cacheKey, sourceService));
    }
}
