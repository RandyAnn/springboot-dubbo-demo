package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonRedisConfig.class, CommonJwtConfig.class})
public class CommonAutoConfiguration {
}