package com.example.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * Spring Security上下文工具类
 * 用于方便地获取当前认证用户的信息
 */
public class SecurityContextUtil {

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        return null;
    }

    /**
     * 获取当前用户名
     * @return 当前用户名，如果未认证则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            return (String) detailsMap.get("username");
        }
        
        return null;
    }

    /**
     * 获取当前用户角色（不含ROLE_前缀）
     * @return 当前用户角色，如果未认证则返回null
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            return (String) detailsMap.get("role");
        }
        
        return null;
    }

    /**
     * 获取当前认证对象
     * @return 当前认证对象，如果未认证则返回null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否已认证
     * @return 如果已认证返回true，否则返回false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && getCurrentUserId() != null;
    }

    /**
     * 检查当前用户是否具有指定角色
     * @param role 角色名称（不含ROLE_前缀）
     * @return 如果具有指定角色返回true，否则返回false
     */
    public static boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * 检查当前用户是否为管理员
     * @return 如果是管理员返回true，否则返回false
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查当前用户是否为普通用户
     * @return 如果是普通用户返回true，否则返回false
     */
    public static boolean isUser() {
        return hasRole("USER");
    }

    /**
     * 获取认证详情中的指定字段
     * @param key 字段名
     * @return 字段值，如果不存在则返回null
     */
    public static Object getDetailValue(String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            return detailsMap.get(key);
        }
        
        return null;
    }
}
