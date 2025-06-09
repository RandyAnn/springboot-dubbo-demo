package com.example.shared.event.kafka;

import com.example.shared.event.DomainEvent;
import com.example.shared.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Kafka事件发布器
 * 基于Kafka Topic发布领域事件
 */
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Value("${app.event.kafka.topic:domain-events}")
    private String eventTopic;

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish a null event.");
            return;
        }

        try {
            // 使用事件类型作为分区键，确保同类型事件有序
            String partitionKey = event.getClass().getSimpleName();
            
            log.info("Publishing event of type '{}' with ID '{}' to Kafka topic '{}'",
                     event.getClass().getSimpleName(), event.getEventId(), eventTopic);

            // 发送到Kafka Topic
            ListenableFuture<SendResult<String, DomainEvent>> future = 
                kafkaTemplate.send(eventTopic, partitionKey, event);

            // 添加回调处理
            future.addCallback(new ListenableFutureCallback<SendResult<String, DomainEvent>>() {
                @Override
                public void onSuccess(SendResult<String, DomainEvent> result) {
                    log.debug("Successfully published event: {} to partition: {}, offset: {}",
                             event, result.getRecordMetadata().partition(), 
                             result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Failed to publish event: {}", event, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing event {} to Kafka: {}", event, e.getMessage(), e);
        }
    }
} 