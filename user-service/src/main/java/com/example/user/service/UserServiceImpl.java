package com.example.user.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.entity.User;
import com.example.common.service.UserService;
import com.example.user.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.common.*;

import java.util.concurrent.TimeUnit;

@Service
@DubboService
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserByUsername(String username) {
        // 先从Redis缓存中获取
        User user = (User)redisTemplate.opsForValue().get("user:" + username);
        if (user != null) {
            return user;
        }

        // 缓存中没有，从数据库获取
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        user = userMapper.selectOne(queryWrapper);

        // 存入缓存
        if (user != null) {
            redisTemplate.opsForValue().set("user:" + username, user, 30, TimeUnit.MINUTES);
        }

        return user;
    }

    @Override
    public User createUser(User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);

        // 更新缓存
        redisTemplate.opsForValue().set("user:" + user.getUsername(), user, 30, TimeUnit.MINUTES);

        return user;
    }
}
