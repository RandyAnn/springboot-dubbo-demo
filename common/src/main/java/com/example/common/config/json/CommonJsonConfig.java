package com.example.common.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CommonJsonConfig {

    /**
     * 配置支持Java 8日期时间类型的ObjectMapper（用于HTTP响应，不包含类型信息）
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，以支持LocalDate、LocalDateTime等序列化
        objectMapper.registerModule(new JavaTimeModule());
        // 配置Jackson以处理更多的序列化情况
        objectMapper.findAndRegisterModules();
        // 配置日期时间格式，不使用时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // HTTP响应不需要类型信息
        return objectMapper;
    }
} 