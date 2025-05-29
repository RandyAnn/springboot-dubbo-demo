package com.example.common.event;

/**
 * 事件监听容器接口
 * 负责管理事件监听器的注册和启动
 */
public interface EventListenerContainer {
    
    /**
     * 注册消息处理器
     * 
     * @param handler 消息处理器
     */
    void registerHandler(MessageHandler handler);
    
    /**
     * 启动事件监听
     */
    void start();
    
    /**
     * 停止事件监听
     */
    void stop();
}
