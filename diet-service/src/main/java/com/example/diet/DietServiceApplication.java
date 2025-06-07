package com.example.diet;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.example.diet.service")
@MapperScan("com.example.diet.mapper")
@EnableCaching
@EnableAsync
public class DietServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DietServiceApplication.class, args);
    }
}