package com.example.shared.config.event;

import com.example.shared.event.EventListenerContainer;
import com.example.shared.event.EventPublisher;
import com.example.shared.event.MessageHandler;
import com.example.shared.event.kafka.KafkaEventListenerContainer;
import com.example.shared.event.kafka.KafkaEventPublisher;
import com.example.shared.event.redis.RedisEventListenerContainer;
import com.example.shared.event.redis.RedisEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableKafka
public class SharedEventConfig {

    // ==================== Redis 配置 ====================

    /**
     * Redis Pub/Sub 事件发布器
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.provider", havingValue = "redis", matchIfMissing = true)
    public EventPublisher redisEventPublisher(@Qualifier("eventRedisTemplate") RedisTemplate<String, Object> eventRedisTemplate,
                                            @Qualifier("eventObjectMapper") ObjectMapper eventObjectMapper) {
        return new RedisEventPublisher(eventRedisTemplate, eventObjectMapper);
    }

    /**
     * 配置 RedisMessageListenerContainer，用于监听Redis频道消息
     * 只有消费者服务才需要
     */
    @Bean
    @ConditionalOnExpression("'${app.event.provider:redis}'.equals('redis') && '${app.event.consumer.enabled:false}'.equals('true')")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("messageListenerExecutor") Executor messageListenerExecutor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(messageListenerExecutor);
        return container;
    }

    /**
     * Redis Pub/Sub 事件监听容器
     * 只有消费者服务才需要
     */
    @Bean
    @ConditionalOnExpression("'${app.event.provider:redis}'.equals('redis') && '${app.event.consumer.enabled:false}'.equals('true')")
    public EventListenerContainer redisEventListenerContainer(
            RedisMessageListenerContainer redisContainer,
            @Qualifier("eventObjectMapper") ObjectMapper eventObjectMapper,
            @Qualifier("eventRedisTemplate") RedisTemplate<String, Object> eventRedisTemplate,
            List<MessageHandler> messageHandlers) {

        RedisEventListenerContainer container = new RedisEventListenerContainer(
            redisContainer, eventObjectMapper, eventRedisTemplate);

        if (messageHandlers != null) {
            messageHandlers.forEach(container::registerHandler);
        }

        return container;
    }

    // ==================== Kafka 配置 ====================

    /**
     * Kafka Producer Factory - 发布者需要
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.provider", havingValue = "kafka")
    public ProducerFactory<String, Object> kafkaProducerFactory(KafkaProperties kafkaProperties,
                                                               @Qualifier("eventObjectMapper") ObjectMapper eventObjectMapper) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        // 配置JsonSerializer使用事件专用ObjectMapper
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(eventObjectMapper);

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(jsonSerializer);

        return factory;
    }

    /**
     * Kafka Template - 发布者需要
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.provider", havingValue = "kafka")
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Kafka Consumer Factory - 只有消费者需要
     */
    @Bean
    @ConditionalOnExpression("'${app.event.provider:redis}'.equals('kafka') && '${app.event.consumer.enabled:false}'.equals('true')")
    public ConsumerFactory<String, Object> kafkaConsumerFactory(KafkaProperties kafkaProperties,
                                                               @Qualifier("eventObjectMapper") ObjectMapper eventObjectMapper) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        // 配置JsonDeserializer使用事件专用ObjectMapper
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(eventObjectMapper);
        // 设置信任的包
        jsonDeserializer.addTrustedPackages("com.example.shared.event", "com.example.diet.event", "com.example.nutrition.event");

        DefaultKafkaConsumerFactory<String, Object> factory = new DefaultKafkaConsumerFactory<>(props);
        factory.setValueDeserializer(jsonDeserializer);

        return factory;
    }

    /**
     * Kafka Listener Container Factory - 只有消费者需要
     */
    @Bean
    @ConditionalOnExpression("'${app.event.provider:redis}'.equals('kafka') && '${app.event.consumer.enabled:false}'.equals('true')")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // 并发配置 - 可以通过配置文件控制
        factory.setConcurrency(3);

        // 错误处理
        factory.setErrorHandler(new SeekToCurrentErrorHandler());

        return factory;
    }

    /**
     * Kafka事件发布器
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.provider", havingValue = "kafka")
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, com.example.shared.event.DomainEvent> typedTemplate =
            (KafkaTemplate<String, com.example.shared.event.DomainEvent>) (KafkaTemplate<?, ?>) kafkaTemplate;
        return new KafkaEventPublisher(typedTemplate);
    }

    /**
     * Kafka事件监听容器 - 只有消费者需要
     */
    @Bean
    @ConditionalOnExpression("'${app.event.provider:redis}'.equals('kafka') && '${app.event.consumer.enabled:false}'.equals('true')")
    public EventListenerContainer kafkaEventListenerContainer(List<MessageHandler> messageHandlers) {
        KafkaEventListenerContainer container = new KafkaEventListenerContainer();

        if (messageHandlers != null) {
            messageHandlers.forEach(container::registerHandler);
        }

        return container;
    }

    // ==================== 事件系统专用配置 ====================

    /**
     * 配置专门用于事件系统的ObjectMapper
     * Redis和Kafka事件都使用此配置，确保序列化格式一致
     */
    @Bean
    public ObjectMapper eventObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 自动发现并注册模块
        objectMapper.findAndRegisterModules();
        // 不使用时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 关键：使用activateDefaultTyping来处理多态类型
        // 这样GenericJackson2JsonRedisSerializer就能正确反序列化事件对象
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
    }

    /**
     * 配置专门用于事件系统的RedisTemplate
     * 与缓存用的RedisTemplate分离，使用事件专用的序列化配置
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.provider", havingValue = "redis", matchIfMissing = true)
    public RedisTemplate<String, Object> eventRedisTemplate(RedisConnectionFactory connectionFactory,
                                                           @Qualifier("eventObjectMapper") ObjectMapper eventObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value序列化：使用GenericJackson2JsonRedisSerializer + 事件专用ObjectMapper
        // 这样可以正确处理多态类型，避免反序列化成LinkedHashMap
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(eventObjectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    // ==================== 通用配置 ====================

    /**
     * 为消息监听器提供专用线程池
     * 只有消费者服务才需要
     */
    @Bean
    @ConditionalOnProperty(name = "app.event.consumer.enabled", havingValue = "true")
    public Executor messageListenerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("event-listener-");
        executor.setRejectedExecutionHandler((r, exec) -> {
            System.err.println("Task rejected from event-listener: " + r.toString());
        });
        executor.initialize();
        return executor;
    }
}
