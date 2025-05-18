package com.example.common.config.exception;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan(basePackages = "com.example.common.exception")
public class GlobalExceptionAutoConfig {
}
