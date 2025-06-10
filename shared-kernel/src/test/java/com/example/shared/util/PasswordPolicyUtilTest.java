package com.example.shared.util;

import com.example.shared.config.properties.PasswordPolicyProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码策略验证工具类测试
 */
public class PasswordPolicyUtilTest {

    @Test
    public void testValidatePassword_Success() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setMinLength(8);
        policy.setRequireUppercase(true);
        policy.setRequireLowercase(true);
        policy.setRequireDigit(true);
        policy.setRequireSpecialChar(true);

        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword("Password123!", policy);

        assertTrue(result.isValid());
        assertTrue(result.getErrorMessages().isEmpty());
    }

    @Test
    public void testValidatePassword_TooShort() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setMinLength(8);

        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword("Pass1!", policy);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("密码长度不能少于8位"));
    }

    @Test
    public void testValidatePassword_MissingUppercase() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setRequireUppercase(true);

        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword("password123!", policy);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("密码必须包含至少一个大写字母"));
    }

    @Test
    public void testValidatePassword_CommonPassword() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setForbidCommonPasswords(true);

        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword("password", policy);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("不能使用常见的弱密码"));
    }

    @Test
    public void testGenerateSecurePassword() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setMinLength(12);
        policy.setRequireUppercase(true);
        policy.setRequireLowercase(true);
        policy.setRequireDigit(true);
        policy.setRequireSpecialChar(true);

        String password = PasswordPolicyUtil.generateSecurePassword(policy);

        assertNotNull(password);
        assertTrue(password.length() >= 12);

        // 验证生成的密码符合策略
        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword(password, policy);
        assertTrue(result.isValid());
    }

    @Test
    public void testEvaluatePasswordStrength() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();

        // 测试强密码
        PasswordPolicyUtil.PasswordStrength strength = 
            PasswordPolicyUtil.evaluatePasswordStrength("StrongPassword123!", policy);
        assertEquals(PasswordPolicyUtil.PasswordStrength.STRONG, strength);

        // 测试弱密码
        strength = PasswordPolicyUtil.evaluatePasswordStrength("weak", policy);
        assertEquals(PasswordPolicyUtil.PasswordStrength.VERY_WEAK, strength);
    }

    @Test
    public void testValidatePassword_DisabledPolicy() {
        PasswordPolicyProperties policy = new PasswordPolicyProperties();
        policy.setEnabled(false);

        PasswordPolicyUtil.ValidationResult result = 
            PasswordPolicyUtil.validatePassword("weak", policy);

        assertTrue(result.isValid());
    }
}
