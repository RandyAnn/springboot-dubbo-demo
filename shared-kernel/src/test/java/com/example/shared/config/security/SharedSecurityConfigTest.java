package com.example.shared.config.security;

import com.example.shared.config.properties.PasswordPolicyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 共享安全配置类测试
 */
public class SharedSecurityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SharedSecurityConfig.class);

    @Test
    public void testPasswordPolicyPropertiesAutoConfiguration() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordPolicyProperties.class);
                    PasswordPolicyProperties properties = context.getBean(PasswordPolicyProperties.class);

                    // 验证Bean能够正确创建
                    assertThat(properties).isNotNull();
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getMinLength()).isEqualTo(8);
                });
    }

    @Test
    public void testDefaultValues() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordPolicyProperties.class);
                    PasswordPolicyProperties properties = context.getBean(PasswordPolicyProperties.class);

                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getMinLength()).isEqualTo(8);
                    assertThat(properties.getMaxLength()).isEqualTo(128);
                    assertThat(properties.isRequireUppercase()).isTrue();
                    assertThat(properties.isRequireLowercase()).isTrue();
                    assertThat(properties.isRequireDigit()).isTrue();
                    assertThat(properties.isRequireSpecialChar()).isTrue();
                    assertThat(properties.getSpecialChars()).isEqualTo("!@#$%^&*()_+-=[]{}|;:,.<>?");
                });
    }

    @Test
    public void testConfigurationBeanExists() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordPolicyProperties.class);
                    assertThat(context).hasSingleBean(SharedSecurityConfig.class);
                });
    }
}
