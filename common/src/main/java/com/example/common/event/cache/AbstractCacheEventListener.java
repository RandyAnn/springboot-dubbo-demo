package com.example.common.event.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 抽象缓存事件监听器
 * 提供基本的事件处理逻辑和过滤功能
 */
public abstract class AbstractCacheEventListener implements CacheEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheEventListener.class);

    /**
     * 当前服务名称
     */
    @Value("${spring.application.name:unknown-service}")
    private String currentServiceName;

    /**
     * 关注的缓存名称集合
     */
    private final Set<String> interestedCacheNames = new HashSet<>();

    /**
     * 构造函数
     *
     * @param cacheNames 关注的缓存名称
     */
    protected AbstractCacheEventListener(String... cacheNames) {
        if (cacheNames != null && cacheNames.length > 0) {
            interestedCacheNames.addAll(Arrays.asList(cacheNames));
        }
    }

    @Override
    public boolean supports(CacheEvent event) {
        // 忽略自己发出的事件，避免循环处理
        if (currentServiceName.equals(event.getSourceService())) {
            return false;
        }

        // 如果没有指定关注的缓存名称，则支持所有缓存
        if (interestedCacheNames.isEmpty()) {
            return true;
        }

        // 只支持关注的缓存名称
        return interestedCacheNames.contains(event.getCacheName());
    }

    @Override
    public void onEvent(CacheEvent event) {
        switch (event.getEventType()) {
            case EVICT:
                handleEvictEvent(event);
                break;
            case CLEAR:
                handleClearEvent(event);
                break;
            case UPDATE:
                handleUpdateEvent(event);
                break;
            case PUT:
                handlePutEvent(event);
                break;
            default:
                logger.warn("未知的缓存事件类型: {}", event.getEventType());
        }
    }

    /**
     * 处理清除特定缓存键的事件
     *
     * @param event 缓存事件
     */
    protected abstract void handleEvictEvent(CacheEvent event);

    /**
     * 处理清除整个缓存的事件
     *
     * @param event 缓存事件
     */
    protected abstract void handleClearEvent(CacheEvent event);

    /**
     * 处理更新缓存的事件
     *
     * @param event 缓存事件
     */
    protected abstract void handleUpdateEvent(CacheEvent event);

    /**
     * 处理添加缓存的事件
     *
     * @param event 缓存事件
     */
    protected abstract void handlePutEvent(CacheEvent event);
}
