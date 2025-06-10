package com.example.shared.util;

import com.example.shared.config.properties.PasswordPolicyProperties;
import lombok.Data;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码策略验证工具类
 * 提供密码复杂度验证、安全密码生成等功能
 */
public class PasswordPolicyUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    
    // 字符集定义
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "0123456789";
    
    // 正则表达式模式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");

    /**
     * 验证密码是否符合策略要求
     *
     * @param password 待验证的密码
     * @param policy   密码策略配置
     * @return 验证结果
     */
    public static ValidationResult validatePassword(String password, PasswordPolicyProperties policy) {
        if (policy == null) {
            return ValidationResult.success();
        }

        // 如果策略未启用，直接通过验证
        if (!policy.isEnabled()) {
            return ValidationResult.success();
        }

        List<String> errors = new ArrayList<>();

        // 检查密码是否为空
        if (password == null || password.isEmpty()) {
            errors.add("密码不能为空");
            return ValidationResult.failure(errors);
        }

        // 检查密码长度
        if (password.length() < policy.getMinLength()) {
            errors.add(String.format("密码长度不能少于%d位", policy.getMinLength()));
        }
        if (password.length() > policy.getMaxLength()) {
            errors.add(String.format("密码长度不能超过%d位", policy.getMaxLength()));
        }

        // 检查大写字母
        if (policy.isRequireUppercase() && !UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个大写字母");
        }

        // 检查小写字母
        if (policy.isRequireLowercase() && !LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个小写字母");
        }

        // 检查数字
        if (policy.isRequireDigit() && !DIGIT_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个数字");
        }

        // 检查特殊字符
        if (policy.isRequireSpecialChar()) {
            boolean hasSpecialChar = false;
            String specialChars = policy.getSpecialChars();
            for (char c : password.toCharArray()) {
                if (specialChars.indexOf(c) >= 0) {
                    hasSpecialChar = true;
                    break;
                }
            }
            if (!hasSpecialChar) {
                errors.add("密码必须包含至少一个特殊字符：" + specialChars);
            }
        }

        // 检查禁用字符
        String forbiddenChars = policy.getForbiddenChars();
        if (forbiddenChars != null && !forbiddenChars.isEmpty()) {
            for (char c : password.toCharArray()) {
                if (forbiddenChars.indexOf(c) >= 0) {
                    errors.add("密码不能包含禁用字符：" + c);
                    break;
                }
            }
        }

        // 检查常见弱密码
        if (policy.isForbidCommonPasswords()) {
            String lowerPassword = password.toLowerCase();
            for (String commonPassword : policy.getCommonPasswords()) {
                if (lowerPassword.equals(commonPassword.toLowerCase())) {
                    errors.add("不能使用常见的弱密码");
                    break;
                }
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * 生成符合策略要求的安全密码
     *
     * @param policy 密码策略配置
     * @return 生成的安全密码
     */
    public static String generateSecurePassword(PasswordPolicyProperties policy) {
        if (policy == null) {
            // 使用默认策略生成密码
            return generateDefaultPassword();
        }

        int length = Math.max(policy.getMinLength(), 12); // 至少12位
        StringBuilder password = new StringBuilder();
        StringBuilder charPool = new StringBuilder();

        // 构建字符池
        if (policy.isRequireLowercase()) {
            charPool.append(LOWERCASE_CHARS);
            password.append(getRandomChar(LOWERCASE_CHARS));
        }
        if (policy.isRequireUppercase()) {
            charPool.append(UPPERCASE_CHARS);
            password.append(getRandomChar(UPPERCASE_CHARS));
        }
        if (policy.isRequireDigit()) {
            charPool.append(DIGIT_CHARS);
            password.append(getRandomChar(DIGIT_CHARS));
        }
        if (policy.isRequireSpecialChar()) {
            String specialChars = policy.getSpecialChars();
            charPool.append(specialChars);
            password.append(getRandomChar(specialChars));
        }

        // 如果字符池为空，使用默认字符集
        if (charPool.length() == 0) {
            charPool.append(LOWERCASE_CHARS).append(UPPERCASE_CHARS).append(DIGIT_CHARS);
        }

        // 填充剩余长度
        while (password.length() < length) {
            password.append(getRandomChar(charPool.toString()));
        }

        // 打乱密码字符顺序
        return shuffleString(password.toString());
    }

    /**
     * 评估密码强度
     *
     * @param password 密码
     * @param policy   密码策略配置
     * @return 密码强度等级
     */
    public static PasswordStrength evaluatePasswordStrength(String password, PasswordPolicyProperties policy) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.VERY_WEAK;
        }

        int score = 0;

        // 长度评分
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // 字符类型评分
        if (UPPERCASE_PATTERN.matcher(password).matches()) score++;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score++;
        if (DIGIT_PATTERN.matcher(password).matches()) score++;

        // 特殊字符评分
        if (policy != null && policy.getSpecialChars() != null) {
            for (char c : password.toCharArray()) {
                if (policy.getSpecialChars().indexOf(c) >= 0) {
                    score++;
                    break;
                }
            }
        }

        // 根据配置的阈值判断强度
        if (policy != null && policy.getStrength() != null) {
            PasswordPolicyProperties.Strength strength = policy.getStrength();
            if (score >= strength.getStrongThreshold()) {
                return PasswordStrength.STRONG;
            } else if (score >= strength.getMediumThreshold()) {
                return PasswordStrength.MEDIUM;
            } else if (score >= strength.getWeakThreshold()) {
                return PasswordStrength.WEAK;
            }
        } else {
            // 使用默认阈值
            if (score >= 4) return PasswordStrength.STRONG;
            if (score >= 3) return PasswordStrength.MEDIUM;
            if (score >= 2) return PasswordStrength.WEAK;
        }

        return PasswordStrength.VERY_WEAK;
    }

    /**
     * 生成默认密码（当策略为空时使用）
     */
    private static String generateDefaultPassword() {
        StringBuilder password = new StringBuilder();
        String allChars = LOWERCASE_CHARS + UPPERCASE_CHARS + DIGIT_CHARS + "!@#$%^&*";
        
        // 确保包含各种字符类型
        password.append(getRandomChar(LOWERCASE_CHARS));
        password.append(getRandomChar(UPPERCASE_CHARS));
        password.append(getRandomChar(DIGIT_CHARS));
        password.append(getRandomChar("!@#$%^&*"));
        
        // 填充到12位
        while (password.length() < 12) {
            password.append(getRandomChar(allChars));
        }
        
        return shuffleString(password.toString());
    }

    /**
     * 从字符集中随机选择一个字符
     */
    private static char getRandomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    /**
     * 打乱字符串顺序
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * 密码验证结果类
     */
    @Data
    public static class ValidationResult {
        private boolean valid;
        private List<String> errorMessages;

        private ValidationResult(boolean valid, List<String> errorMessages) {
            this.valid = valid;
            this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public static ValidationResult failure(String error) {
            return new ValidationResult(false, Arrays.asList(error));
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessages.isEmpty() ? "" : String.join("; ", errorMessages);
        }
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        VERY_WEAK("非常弱"),
        WEAK("弱"),
        MEDIUM("中等"),
        STRONG("强");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
