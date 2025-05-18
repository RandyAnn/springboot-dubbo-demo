package com.example.common.config;

import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.config.event.CacheEventConfig;
import com.example.common.config.jwt.CommonJwtConfig;
import com.example.common.config.exception.GlobalExceptionAutoConfig;
import com.example.common.config.mbplus.CommonMybatisPlusConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    CommonJwtConfig.class,
    CommonMybatisPlusConfig.class,
    CommonCacheConfig.class,
    CacheEventConfig.class, GlobalExceptionAutoConfig.class
})
public class CommonAutoConfiguration {
}