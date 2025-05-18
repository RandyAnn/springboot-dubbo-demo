package com.example.common.event.cache;

/**
 * 缓存事件类型枚举
 * 定义所有允许的缓存事件类型
 */
public enum CacheEventType {
    /**
     * 清除特定缓存键
     */
    EVICT,
    
    /**
     * 清除整个缓存
     */
    CLEAR,
    
    /**
     * 更新缓存
     */
    UPDATE,
    
    /**
     * 添加缓存
     */
    PUT
}
