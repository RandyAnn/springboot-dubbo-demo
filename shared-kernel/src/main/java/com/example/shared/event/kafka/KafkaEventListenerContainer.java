package com.example.shared.event.kafka;

import com.example.shared.event.DomainEvent;
import com.example.shared.event.DomainEventHandler;
import com.example.shared.event.EventListenerContainer;
import com.example.shared.event.MessageHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kafka事件监听容器
 * 基于Kafka Topic监听和处理领域事件
 */
public class KafkaEventListenerContainer implements EventListenerContainer, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventListenerContainer.class);

    private final List<MessageHandler> handlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerHandler(MessageHandler handler) {
        if (handler != null) {
            this.handlers.add(handler);
            log.info("Registered MessageHandler: {}", handler.getClass().getName());
        }
    }

    /**
     * Kafka监听器方法
     * 使用配置文件中的topic和group-id
     */
    @KafkaListener(topics = "${app.event.kafka.topic:domain-events}", 
                   groupId = "${spring.kafka.consumer.group-id:default-group}")
    public void handleEvent(ConsumerRecord<String, DomainEvent> record) {
        try {
            DomainEvent event = record.value();
            String topic = record.topic();
            int partition = record.partition();
            long offset = record.offset();

            log.debug("Received event from Kafka topic '{}', partition {}, offset {}", 
                     topic, partition, offset);

            if (event != null) {
                log.info("Processing event of type '{}' with ID '{}' from Kafka",
                         event.getClass().getSimpleName(), event.getEventId());

                // 与Redis实现相同的Handler调用逻辑
                for (MessageHandler handler : handlers) {
                    try {
                        if (handler instanceof DomainEventHandler) {
                            @SuppressWarnings("rawtypes")
                            DomainEventHandler domainEventHandler = (DomainEventHandler) handler;
                            if (domainEventHandler.supports(event.getClass())) {
                                log.debug("Dispatching event {} to handler {}", 
                                         event.getClass().getSimpleName(), 
                                         handler.getClass().getName());
                                domainEventHandler.onMessage(event);
                            }
                        } else {
                            // 对于非 DomainEventHandler 的通用 MessageHandler，直接调用
                            log.debug("Dispatching event {} to generic handler {}", 
                                     event.getClass().getSimpleName(), 
                                     handler.getClass().getName());
                            handler.onMessage(event);
                        }
                    } catch (Exception e) {
                        log.error("Error processing event {} with handler {}: {}",
                                  event, handler.getClass().getName(), e.getMessage(), e);
                        // 单个处理器异常不应影响其他处理器
                    }
                }
            } else {
                log.warn("Received null event from Kafka topic '{}'", topic);
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void start() {
        log.info("KafkaEventListenerContainer started.");
    }

    @Override
    public void stop() {
        log.info("KafkaEventListenerContainer stopped.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
        log.info("Subscribed to Kafka topic: ${app.event.kafka.topic:domain-events}");
    }
} 