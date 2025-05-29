package com.example.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件抽象基类
 * 定义所有领域事件的通用属性和行为
 */
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
    
    /**
     * 获取事件唯一标识
     * 
     * @return 事件ID
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * 获取事件发生时间戳
     * 
     * @return 时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取聚合根ID
     * 
     * @return 聚合根ID
     */
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * 获取事件版本
     * 
     * @return 事件版本
     */
    public Long getVersion() {
        return version;
    }
    
    /**
     * 获取事件来源服务
     * 
     * @return 来源服务
     */
    public String getSource() {
        return source;
    }
    
    /**
     * 获取事件类型名称
     * 
     * @return 事件类型名称
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                ", source='" + source + '\'' +
                ", eventType='" + getEventType() + '\'' +
                '}';
    }
}
