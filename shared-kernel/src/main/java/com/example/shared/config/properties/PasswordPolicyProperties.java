package com.example.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 密码策略配置属性类
 * 统一管理密码安全策略的所有配置项，包括密码复杂度要求、长度限制等配置
 */
@Data
@ConfigurationProperties(prefix = "app.security.password")
public class PasswordPolicyProperties {

    /**
     * 是否启用密码策略验证
     */
    private boolean enabled = true;

    /**
     * 密码最小长度
     */
    private int minLength = 8;

    /**
     * 密码最大长度
     */
    private int maxLength = 128;

    /**
     * 是否要求包含大写字母
     */
    private boolean requireUppercase = true;

    /**
     * 是否要求包含小写字母
     */
    private boolean requireLowercase = true;

    /**
     * 是否要求包含数字
     */
    private boolean requireDigit = true;

    /**
     * 是否要求包含特殊字符
     */
    private boolean requireSpecialChar = true;

    /**
     * 允许的特殊字符集合
     */
    private String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    /**
     * 密码不能包含的字符（如空格）
     */
    private String forbiddenChars = " ";

    /**
     * 是否禁止使用常见弱密码
     */
    private boolean forbidCommonPasswords = true;

    /**
     * 常见弱密码列表
     */
    private String[] commonPasswords = {
        "password", "123456", "123456789", "12345678", "12345",
        "1234567", "admin", "qwerty", "abc123", "password123",
        "123123", "111111", "000000", "iloveyou", "welcome"
    };

    /**
     * 密码强度等级配置
     */
    private Strength strength = new Strength();

    /**
     * 密码强度等级配置
     */
    @Data
    public static class Strength {
        /**
         * 弱密码阈值（满足条件数量）
         */
        private int weakThreshold = 2;

        /**
         * 中等密码阈值（满足条件数量）
         */
        private int mediumThreshold = 3;

        /**
         * 强密码阈值（满足条件数量）
         */
        private int strongThreshold = 4;
    }
}
