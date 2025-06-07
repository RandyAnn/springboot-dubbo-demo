package com.example.shared.event;

/**
 * 领域事件处理器接口
 * 提供类型安全的事件处理能力，方便业务侧按类型分发事件
 * 
 * @param <E> 领域事件类型
 */
public interface DomainEventHandler<E extends DomainEvent> extends MessageHandler {
    
    /**
     * 判断是否支持处理指定类型的事件
     * 
     * @param eventType 事件类型
     * @return 是否支持处理
     */
    boolean supports(Class<? extends DomainEvent> eventType);
    
    /**
     * 处理特定类型的领域事件
     * 
     * @param event 领域事件
     */
    void handle(E event);
    
    /**
     * 默认的消息处理实现
     * 根据事件类型进行分发
     * 
     * @param event 领域事件
     */
    @Override
    default void onMessage(DomainEvent event) {
        if (supports(event.getClass())) {
            @SuppressWarnings("unchecked")
            E typedEvent = (E) event;
            handle(typedEvent);
        }
    }
}
