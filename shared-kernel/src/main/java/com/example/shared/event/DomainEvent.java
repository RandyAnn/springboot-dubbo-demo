package com.example.shared.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件抽象基类
 * 定义所有领域事件的通用属性和行为
 */
@Getter
public abstract class DomainEvent {

    /**
     * 事件唯一标识
     */
    private final String eventId = UUID.randomUUID().toString();

    /**
     * 事件发生时间戳
     */
    private final Instant timestamp = Instant.now();

    /**
     * 聚合根ID
     */
    private final String aggregateId;

    /**
     * 事件版本
     */
    private final Long version;

    /**
     * 事件来源服务
     */
    private final String source;

    /**
     * 构造函数
     *
     * @param aggregateId 聚合根ID
     */
    protected DomainEvent(String aggregateId) {
        this(aggregateId, 1L, null);
    }

    /**
     * 构造函数
     *
     * @param aggregateId 聚合根ID
     * @param version 事件版本
     * @param source 事件来源服务
     */
    protected DomainEvent(String aggregateId, Long version, String source) {
        this.aggregateId = aggregateId;
        this.version = version;
        this.source = source;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                ", source='" + source + '\'' +
                '}';
    }
}
