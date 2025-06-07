package com.example.shared.config.exception;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan(basePackages = "com.example.shared.exception")
public class GlobalExceptionAutoConfig {
}
