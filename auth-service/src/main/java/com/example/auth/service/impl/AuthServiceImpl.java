package com.example.auth.service;

import com.example.common.dto.LoginRequest;
import com.example.common.dto.LoginResponse;
import com.example.common.dto.WechatLoginRequest;
import com.example.common.entity.User;
import com.example.common.exception.BusinessException;
import com.example.common.service.UserService;
import com.example.common.util.JwtUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@DubboService
public class AuthServiceImpl implements AuthService {

    @DubboReference
    private UserService userService;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    
    @Value("${wechat.appid}")
    private String appId;
    
    @Value("${wechat.secret}")
    private String appSecret;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userService.getUserByUsername(request.getUsername());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401,"用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(claims, user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());

        return response;
    }

    @Override
    public void logout(String token) {
        jwtUtil.blacklistToken(token);
    }

    @Override
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        // 1. 通过code获取微信access_token和openid
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=" + appId +
                "&secret=" + appSecret +
                "&code=" + request.getCode() +
                "&grant_type=authorization_code";
        
        ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> tokenInfo = tokenResponse.getBody();
        
        if (tokenInfo.containsKey("errcode")) {
            throw new BusinessException(400, "微信授权失败: " + tokenInfo.get("errmsg"));
        }
        
        String openid = (String) tokenInfo.get("openid");
        String accessToken = (String) tokenInfo.get("access_token");
        String unionid = tokenInfo.containsKey("unionid") ? (String) tokenInfo.get("unionid") : null;
        
        // 2. 获取微信用户信息
        String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                "?access_token=" + accessToken +
                "&openid=" + openid +
                "&lang=zh_CN";
        
        ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
        Map<String, Object> userInfo = userInfoResponse.getBody();
        
        if (userInfo.containsKey("errcode")) {
            throw new BusinessException(400, "获取微信用户信息失败: " + userInfo.get("errmsg"));
        }
        
        String nickname = (String) userInfo.get("nickname");
        String avatarUrl = (String) userInfo.get("headimgurl");
        
        // 3. 查询用户是否已存在
        User user = userService.getUserByOpenid(openid);
        
        // 4. 用户不存在则创建新用户
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setUsername("wx_" + UUID.randomUUID().toString().substring(0, 8));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 随机密码
            user.setRole("USER");
            user.setStatus(1); // 默认启用
            user.setLoginType(1); // 微信登录
            user.setCreateTime(new Date());
            
            user = userService.createUser(user);
        }
        
        // 5. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }
        
        // 6. 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        
        String token = jwtUtil.generateToken(claims, user.getUsername());
        
        // 7. 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        
        return response;
    }
}
