package com.example.auth.service.impl;

import com.example.auth.service.AuthService;
import com.example.user.command.UserCreateCommand;
import com.example.user.dto.LoginRequestDTO;
import com.example.user.dto.LoginResponseDTO;
import com.example.user.dto.PasswordChangeRequestDTO;
import com.example.user.dto.UserInfoDTO;
import com.example.user.dto.WechatLoginRequestDTO;
import com.example.shared.exception.BusinessException;
import com.example.user.service.UserService;
import com.example.shared.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@DubboService
public class AuthServiceImpl implements AuthService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String appSecret;

    @Value("${wechat.login-url}")
    private String loginUrl;

    @DubboReference
    private UserService userService;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginResponseDTO adminLogin(LoginRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }

        UserInfoDTO userInfo = userService.getUserByUsername(request.getUsername());

        if (userInfo == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证用户密码
        if (!userService.verifyPassword(request.getUsername(), request.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证用户角色
        if (!"ADMIN".equals(userInfo.getRole())) {
            throw new BusinessException(403, "该账号不是管理员账号");
        }

        // 检查用户状态
        if (userInfo.getStatus() != null && userInfo.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        return generateToken(userInfo);
    }

    @Override
    public LoginResponseDTO userLogin(LoginRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException(400, "邮箱不能为空");
        }

        UserInfoDTO userInfo = userService.getUserByEmail(request.getEmail());

        if (userInfo == null) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        // 验证用户密码
        if (!userService.verifyPassword(request.getEmail(), request.getPassword())) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        // 验证用户角色
        if (!"USER".equals(userInfo.getRole())) {
            throw new BusinessException(403, "该账号不是普通用户账号");
        }

        // 检查用户状态
        if (userInfo.getStatus() != null && userInfo.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        return generateToken(userInfo);
    }

    @Override
    public LoginResponseDTO wechatLogin(WechatLoginRequestDTO request) {
        try {
            // 构建请求微信API的URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    loginUrl, appId, appSecret, request.getCode());

            // 发送请求到微信API
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // 解析微信返回的JSON数据
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 检查微信返回是否有错误
            if (rootNode.has("errcode") && rootNode.get("errcode").asInt() != 0) {
                throw new BusinessException(401, "微信登录失败: " + rootNode.get("errmsg").asText());
            }

            // 获取用户的openid
            String openid = rootNode.get("openid").asText();

            // 通过openid查询用户
            UserInfoDTO userInfo = userService.getUserByOpenid(openid);

            // 如果用户不存在，创建新用户
            if (userInfo == null) {
                // 创建UserCreateCommand对象
                UserCreateCommand command = new UserCreateCommand();
                // 生成随机用户名
                command.setUsername("wx_" + UUID.randomUUID().toString().substring(0, 8));
                // 设置随机密码
                String randomPassword = UUID.randomUUID().toString();
                command.setPassword(randomPassword); // 密码会在service层加密
                // 设置角色
                command.setRole("USER");
                // 设置email避免数据库错误
                command.setEmail(openid + "@wx.placeholder.com");
                // 保存openid
                command.setOpenid(openid);

                // 创建用户
                userInfo = userService.createUser(command);
            }

            // 检查用户状态
            if (userInfo.getStatus() != null && userInfo.getStatus() == 0) {
                throw new BusinessException(403, "账号已被封禁，请联系管理员");
            }

            // 生成token并返回
            return generateToken(userInfo);

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(500, "微信登录过程中发生错误: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        jwtUtil.blacklistToken(token);
    }

    /**
     * 修改用户密码
     */
    @Override
    public boolean changePassword(PasswordChangeRequestDTO request) throws BusinessException {
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }

        if (request.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw new BusinessException(400, "旧密码不能为空");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new BusinessException(400, "新密码不能为空");
        }

        // 获取用户信息
        UserInfoDTO userInfo = userService.getUserById(request.getUserId());
        if (userInfo == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码是否正确
        String usernameOrEmail = userInfo.getEmail() != null ? userInfo.getEmail() : userInfo.getUsername();
        if (!userService.verifyPassword(usernameOrEmail, request.getOldPassword())) {
            throw new BusinessException(401, "旧密码不正确");
        }

        // 修改密码
        boolean result = userService.changePassword(request.getUserId(), request.getNewPassword());

        // 如果密码修改成功，则使当前token失效
        // 注意：这里需要在controller层传入当前的token
        if (result && request instanceof PasswordChangeRequestDTO && ((PasswordChangeRequestDTO) request).getToken() != null) {
            logout(((PasswordChangeRequestDTO) request).getToken());
        }

        return result;
    }

    // 修改生成token的方法，直接使用UserInfoDTO
    private LoginResponseDTO generateToken(UserInfoDTO userInfo) {
        // 生成 JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userInfo.getUsername());
        claims.put("role", userInfo.getRole());
        claims.put("userId", userInfo.getId());

        String token = jwtUtil.generateToken(claims, userInfo.getUsername());

        // 构建 LoginResponse
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUserInfo(userInfo);

        return response;
    }
}