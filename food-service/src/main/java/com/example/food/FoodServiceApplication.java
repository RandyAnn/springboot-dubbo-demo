package com.example.food;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 食物服务启动类
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.example.food.service")
@MapperScan("com.example.food.mapper")
public class FoodServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodServiceApplication.class, args);
    }
} 