package com.example.nutrition;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 营养服务启动类
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.example.nutrition.service")
@MapperScan("com.example.nutrition.mapper")
@EnableCaching
@EnableAsync
public class NutritionServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NutritionServiceApplication.class, args);
    }
} 