package com.example.common.event.cache;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 缓存事件结构体
 * 定义缓存事件的标准数据结构
 */
public class CacheEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件类型
     */
    private CacheEventType eventType;
    
    /**
     * 缓存名称
     */
    private String cacheName;
    
    /**
     * 缓存键，可以为null（如清除整个缓存时）
     */
    private String cacheKey;
    
    /**
     * 事件发布时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 事件发布服务名称
     */
    private String sourceService;
    
    /**
     * 默认构造函数（用于序列化）
     */
    public CacheEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 创建一个清除特定缓存键的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     * @return 缓存事件
     */
    public static CacheEvent evictEvent(String cacheName, String cacheKey, String sourceService) {
        CacheEvent event = new CacheEvent();
        event.setEventType(CacheEventType.EVICT);
        event.setCacheName(cacheName);
        event.setCacheKey(cacheKey);
        event.setSourceService(sourceService);
        return event;
    }
    
    /**
     * 创建一个清除整个缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param sourceService 源服务名称
     * @return 缓存事件
     */
    public static CacheEvent clearEvent(String cacheName, String sourceService) {
        CacheEvent event = new CacheEvent();
        event.setEventType(CacheEventType.CLEAR);
        event.setCacheName(cacheName);
        event.setSourceService(sourceService);
        return event;
    }
    
    /**
     * 创建一个更新缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     * @return 缓存事件
     */
    public static CacheEvent updateEvent(String cacheName, String cacheKey, String sourceService) {
        CacheEvent event = new CacheEvent();
        event.setEventType(CacheEventType.UPDATE);
        event.setCacheName(cacheName);
        event.setCacheKey(cacheKey);
        event.setSourceService(sourceService);
        return event;
    }
    
    /**
     * 创建一个添加缓存的事件
     * 
     * @param cacheName 缓存名称
     * @param cacheKey 缓存键
     * @param sourceService 源服务名称
     * @return 缓存事件
     */
    public static CacheEvent putEvent(String cacheName, String cacheKey, String sourceService) {
        CacheEvent event = new CacheEvent();
        event.setEventType(CacheEventType.PUT);
        event.setCacheName(cacheName);
        event.setCacheKey(cacheKey);
        event.setSourceService(sourceService);
        return event;
    }

    // Getters and Setters
    public CacheEventType getEventType() {
        return eventType;
    }

    public void setEventType(CacheEventType eventType) {
        this.eventType = eventType;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    @Override
    public String toString() {
        return "CacheEvent{" +
                "eventType=" + eventType +
                ", cacheName='" + cacheName + '\'' +
                ", cacheKey='" + cacheKey + '\'' +
                ", timestamp=" + timestamp +
                ", sourceService='" + sourceService + '\'' +
                '}';
    }
}
