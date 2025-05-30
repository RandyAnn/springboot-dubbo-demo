package com.example.common.config;

import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.config.event.CommonEventConfig;
import com.example.common.config.json.CommonJsonConfig;
import com.example.common.config.redis.CommonRedisConfig;
import com.example.common.config.jwt.CommonJwtConfig;
import com.example.common.config.exception.GlobalExceptionAutoConfig;
import com.example.common.config.mbplus.CommonMybatisPlusConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    CommonJsonConfig.class,
    CommonRedisConfig.class,
    CommonEventConfig.class,
    CommonJwtConfig.class,
    CommonMybatisPlusConfig.class,
    CommonCacheConfig.class,
    GlobalExceptionAutoConfig.class
})
public class CommonAutoConfiguration {
}