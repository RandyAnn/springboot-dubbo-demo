package com.example.shared.config;

import com.example.shared.config.cache.SharedCacheConfig;
import com.example.shared.config.event.SharedEventConfig;
import com.example.shared.config.json.SharedJsonConfig;
import com.example.shared.config.redis.SharedRedisConfig;
import com.example.shared.config.jwt.SharedJwtConfig;
import com.example.shared.config.exception.GlobalExceptionAutoConfig;
import com.example.shared.config.mbplus.SharedMybatisPlusConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    SharedJsonConfig.class,
    SharedRedisConfig.class,
    SharedEventConfig.class,
    SharedJwtConfig.class,
    SharedMybatisPlusConfig.class,
    SharedCacheConfig.class,
    GlobalExceptionAutoConfig.class
})
public class SharedKernelAutoConfiguration {
}
