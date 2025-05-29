package com.example.common.event;

/**
 * 事件发布器接口
 * 负责发布领域事件到消息中间件
 */
public interface EventPublisher {
    
    /**
     * 发布领域事件
     * 
     * @param event 领域事件
     */
    void publish(DomainEvent event);
}
