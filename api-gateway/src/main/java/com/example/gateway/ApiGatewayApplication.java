package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * API Gateway应用启动类
 * 基于Spring Cloud Gateway的微服务网关
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
