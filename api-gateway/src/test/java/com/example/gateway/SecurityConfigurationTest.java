package com.example.gateway;

import com.example.gateway.security.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 安全配置验证测试
 * 验证重构后的安全策略是否正常工作
 */
@WebFluxTest
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class SecurityConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * 测试不需要认证的路径 - 认证接口
     */
    @Test
    public void testAuthEndpointsPermitAll() {
        // 测试登录接口不需要认证
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // 404表示路由到后端但后端不存在，而不是401认证失败

        // 测试注册接口不需要认证
        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // 404表示路由到后端但后端不存在，而不是401认证失败
    }

    /**
     * 测试不需要认证的路径 - 文件下载
     */
    @Test
    public void testFileDownloadPermitAll() {
        webTestClient.get()
                .uri("/api/files/download/test-file")
                .exchange()
                .expectStatus().isNotFound(); // 404表示路由到后端但后端不存在，而不是401认证失败
    }

    /**
     * 测试不需要认证的路径 - 静态资源
     */
    @Test
    public void testStaticResourcesPermitAll() {
        // 测试根路径
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isNotFound(); // 404表示没有对应的handler，而不是401认证失败

        // 测试静态资源路径
        webTestClient.get()
                .uri("/static/css/app.css")
                .exchange()
                .expectStatus().isNotFound(); // 404表示没有对应的handler，而不是401认证失败

        // 测试公共资源路径
        webTestClient.get()
                .uri("/public/images/logo.png")
                .exchange()
                .expectStatus().isNotFound(); // 404表示没有对应的handler，而不是401认证失败
    }

    /**
     * 测试不需要认证的路径 - 健康检查
     */
    @Test
    public void testActuatorEndpointsPermitAll() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isNotFound(); // 404表示没有对应的actuator，而不是401认证失败
    }

    /**
     * 测试需要认证的路径 - 无token时应该被拦截
     */
    @Test
    public void testProtectedEndpointsRequireAuth() {
        // 测试用户接口需要认证
        webTestClient.get()
                .uri("/api/users/profile")
                .exchange()
                .expectStatus().isUnauthorized(); // 401表示需要认证

        // 测试食物接口需要认证
        webTestClient.get()
                .uri("/api/food/list")
                .exchange()
                .expectStatus().isUnauthorized(); // 401表示需要认证

        // 测试饮食记录接口需要认证
        webTestClient.get()
                .uri("/api/diet-records/list")
                .exchange()
                .expectStatus().isUnauthorized(); // 401表示需要认证

        // 测试营养分析接口需要认证
        webTestClient.get()
                .uri("/api/nutrition/analysis")
                .exchange()
                .expectStatus().isUnauthorized(); // 401表示需要认证
    }

    /**
     * 测试带有无效token的请求
     */
    @Test
    public void testInvalidTokenStillRequiresAuth() {
        webTestClient.get()
                .uri("/api/users/profile")
                .header("Authorization", "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized(); // 401表示token无效，需要认证
    }

    /**
     * 测试其他路径允许访问
     */
    @Test
    public void testOtherPathsPermitAll() {
        webTestClient.get()
                .uri("/some-other-path")
                .exchange()
                .expectStatus().isNotFound(); // 404表示没有对应的handler，而不是401认证失败
    }
}