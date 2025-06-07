package com.example.shared.event;

/**
 * 消息处理器接口
 * 定义处理领域事件消息的通用接口
 */
public interface MessageHandler {
    
    /**
     * 处理领域事件消息
     * 
     * @param event 领域事件
     */
    void onMessage(DomainEvent event);
}
