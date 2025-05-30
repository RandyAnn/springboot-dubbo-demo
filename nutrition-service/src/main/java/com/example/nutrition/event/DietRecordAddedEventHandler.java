package com.example.nutrition.event;

import com.example.common.event.events.DietRecordAddedEvent;
import com.example.common.event.DomainEvent;
import com.example.common.event.DomainEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 饮食记录添加事件处理器
 * 当用户添加饮食记录时，清除相关的营养统计缓存
 */
@Slf4j
@Component
public class DietRecordAddedEventHandler implements DomainEventHandler<DietRecordAddedEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CacheManager cacheManager;

    @Autowired
    public DietRecordAddedEventHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean supports(Class<? extends DomainEvent> eventType) {
        return DietRecordAddedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void handle(DietRecordAddedEvent event) {
        log.info("处理饮食记录添加事件: userId={}, recordId={}, date={}",
            event.getUserId(), event.getDietRecordId(), event.getRecordDate());

        try {
            Long userId = event.getUserId();
            String dateStr = event.getRecordDate().format(DATE_FORMATTER);
            LocalDate today = LocalDate.now();

            // 直接驱逐相关缓存项
            evictCache("nutritionStat", "daily_" + userId + "_" + dateStr);
            evictCache("nutritionStat", "details_" + userId + "_" + dateStr);
            evictCache("nutritionStat", "advice_" + userId + "_" + dateStr);
            evictCache("healthReport", "report_" + userId + "_" + dateStr);

            // 驱逐趋势缓存（只驱逐包含当前记录日期的趋势）
            LocalDate recordDate = event.getRecordDate();
            if (!recordDate.isAfter(today)) {
                // 最近7天
                LocalDate weekStart = today.minus(6, ChronoUnit.DAYS);
                if (!recordDate.isBefore(weekStart)) {
                    evictCache("nutritionStat", "trend_" + userId + "_" + weekStart.format(DATE_FORMATTER) + "_" + today.format(DATE_FORMATTER));
                }

                // 最近30天
                LocalDate monthStart = today.minus(29, ChronoUnit.DAYS);
                if (!recordDate.isBefore(monthStart)) {
                    evictCache("nutritionStat", "trend_" + userId + "_" + monthStart.format(DATE_FORMATTER) + "_" + today.format(DATE_FORMATTER));
                }
            }

            log.info("成功驱逐用户营养统计缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("驱逐用户营养统计缓存失败: userId={}, error={}",
                event.getUserId(), e.getMessage(), e);
        }
    }

    private void evictCache(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("驱逐缓存项: {}::{}", cacheName, key);
            }
        } catch (Exception e) {
            log.warn("驱逐缓存项失败: {}::{}, error: {}", cacheName, key, e.getMessage());
        }
    }
}
