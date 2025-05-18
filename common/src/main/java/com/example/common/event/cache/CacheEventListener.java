package com.example.common.event.cache;

/**
 * 缓存事件监听器接口
 * 定义缓存事件监听的通用接口
 */
public interface CacheEventListener {
    
    /**
     * 处理缓存事件
     * 
     * @param event 缓存事件
     */
    void onEvent(CacheEvent event);
    
    /**
     * 判断是否支持处理该事件
     * 默认实现为支持所有事件，子类可以覆盖此方法以实现选择性处理
     * 
     * @param event 缓存事件
     * @return 是否支持处理该事件
     */
    default boolean supports(CacheEvent event) {
        return true;
    }
}
