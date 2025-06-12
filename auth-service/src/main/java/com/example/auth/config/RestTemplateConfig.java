package com.example.auth.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 专门为auth-service提供HTTP客户端配置，用于调用微信API
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean
     * 包含连接池、超时等优化配置，用于微信API调用
     */
    @Bean
    public RestTemplate restTemplate() {
        // 创建HttpClient，配置连接池
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(200)  // 最大连接数
                .setMaxConnPerRoute(50)  // 每个路由的最大连接数
                .build();

        // 创建请求工厂，配置超时
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5000);  // 连接超时：5秒
        factory.setReadTimeout(10000);    // 读取超时：10秒

        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate(factory);

        return restTemplate;
    }
}