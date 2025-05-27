package com.example.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * 支持二级缓存 + 异步回写的 Spring Cache 实现
 * 读操作：先本地缓存，再远程缓存，回填本地
 * 写操作：立刻更新本地缓存，异步更新远程缓存
 */
public class AsyncTwoLevelCache implements Cache {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTwoLevelCache.class);
    
    private final String name;
    private final Cache localCache;
    private final Cache remoteCache;
    private final Executor executor;
    
    public AsyncTwoLevelCache(String name, Cache localCache, Cache remoteCache, Executor executor) {
        this.name = name;
        this.localCache = localCache;
        this.remoteCache = remoteCache;
        this.executor = executor;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Object getNativeCache() {
        return this;
    }
    
    /**
     * 读取缓存：先本地，再远程，回填本地
     */
    @Override
    public ValueWrapper get(Object key) {
        // 先尝试本地缓存
        ValueWrapper localValue = localCache.get(key);
        if (localValue != null) {
            logger.debug("本地缓存命中: cache={}, key={}", name, key);
            return localValue;
        }
        
        // 本地缓存未命中，尝试远程缓存
        ValueWrapper remoteValue = remoteCache.get(key);
        if (remoteValue != null) {
            logger.debug("远程缓存命中，回填本地缓存: cache={}, key={}", name, key);
            // 回填本地缓存
            localCache.put(key, remoteValue.get());
            return remoteValue;
        }
        
        logger.debug("缓存未命中: cache={}, key={}", name, key);
        return null;
    }
    
    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            Object value = wrapper.get();
            if (value != null && type.isAssignableFrom(value.getClass())) {
                return type.cast(value);
            }
        }
        return null;
    }
    
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }
        
        try {
            T value = valueLoader.call();
            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }
    
    /**
     * 写入缓存：立刻写本地，异步写远程
     */
    @Override
    public void put(Object key, Object value) {
        // 立即更新本地缓存
        localCache.put(key, value);
        logger.debug("本地缓存已更新: cache={}, key={}", name, key);
        
        // 异步更新远程缓存
        executor.execute(() -> {
            try {
                remoteCache.put(key, value);
                logger.debug("远程缓存异步更新完成: cache={}, key={}", name, key);
            } catch (Exception e) {
                logger.error("远程缓存异步更新失败: cache={}, key={}", name, key, e);
            }
        });
    }
    
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        // 先检查本地缓存
        ValueWrapper existing = localCache.get(key);
        if (existing != null) {
            return existing;
        }
        
        // 检查远程缓存
        existing = remoteCache.get(key);
        if (existing != null) {
            // 回填本地缓存
            localCache.put(key, existing.get());
            return existing;
        }
        
        // 都不存在，执行put操作
        put(key, value);
        return null;
    }
    
    /**
     * 清除缓存：本地立即清，远程异步清
     */
    @Override
    public void evict(Object key) {
        // 立即清除本地缓存
        localCache.evict(key);
        logger.debug("本地缓存已清除: cache={}, key={}", name, key);
        
        // 异步清除远程缓存
        executor.execute(() -> {
            try {
                remoteCache.evict(key);
                logger.debug("远程缓存异步清除完成: cache={}, key={}", name, key);
            } catch (Exception e) {
                logger.error("远程缓存异步清除失败: cache={}, key={}", name, key, e);
            }
        });
    }
    
    @Override
    public boolean evictIfPresent(Object key) {
        boolean localEvicted = localCache.evictIfPresent(key);
        
        // 异步清除远程缓存
        executor.execute(() -> {
            try {
                remoteCache.evictIfPresent(key);
                logger.debug("远程缓存异步清除完成: cache={}, key={}", name, key);
            } catch (Exception e) {
                logger.error("远程缓存异步清除失败: cache={}, key={}", name, key, e);
            }
        });
        
        return localEvicted;
    }
    
    /**
     * 清空缓存：本地立即清，远程异步清
     */
    @Override
    public void clear() {
        // 立即清空本地缓存
        localCache.clear();
        logger.debug("本地缓存已清空: cache={}", name);
        
        // 异步清空远程缓存
        executor.execute(() -> {
            try {
                remoteCache.clear();
                logger.debug("远程缓存异步清空完成: cache={}", name);
            } catch (Exception e) {
                logger.error("远程缓存异步清空失败: cache={}", name, e);
            }
        });
    }
    
    @Override
    public boolean invalidate() {
        boolean localInvalidated = localCache.invalidate();
        
        // 异步清空远程缓存
        executor.execute(() -> {
            try {
                remoteCache.invalidate();
                logger.debug("远程缓存异步失效完成: cache={}", name);
            } catch (Exception e) {
                logger.error("远程缓存异步失效失败: cache={}", name, e);
            }
        });
        
        return localInvalidated;
    }
    
    /**
     * 获取本地缓存实例（用于调试和监控）
     */
    public Cache getLocalCache() {
        return localCache;
    }
    
    /**
     * 获取远程缓存实例（用于调试和监控）
     */
    public Cache getRemoteCache() {
        return remoteCache;
    }
}
