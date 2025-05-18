package com.example.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.command.UserUpdateCommand;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.UserInfoDTO;
import com.example.common.response.PageResult;
import com.example.common.entity.User;
import com.example.common.exception.BusinessException;
import com.example.common.service.UserService;
import com.example.user.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    @DubboReference
    private FileService fileService;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRATION_MINUTES = 30;

    @Autowired
    public UserServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           FileService fileService,
                           CacheService cacheService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.cacheService = cacheService;
    }

    @Override
    public UserInfoDTO getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        return convertToDTO(user);
    }

    @Override
    public UserInfoDTO getUserById(Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        User user = userMapper.selectOne(queryWrapper);
        return convertToDTO(user);
    }

    @Override
    public UserInfoDTO getUserByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        return convertToDTO(user);
    }



    @Override
    public UserInfoDTO createUser(UserUpdateCommand command) throws BusinessException {
        if (command == null) {
            throw new BusinessException(400, "创建命令不能为空");
        }

        // 创建用户实体
        User user = new User();

        // 复制非null字段
        if (command.getUsername() != null) user.setUsername(command.getUsername());
        if (command.getEmail() != null) user.setEmail(command.getEmail());
        if (command.getRole() != null) user.setRole(command.getRole());
        if (command.getStatus() != null) user.setStatus(command.getStatus());
        if (command.getAvatarUrl() != null) user.setAvatarUrl(command.getAvatarUrl());
        if (command.getOpenid() != null) user.setOpenid(command.getOpenid());

        // 设置默认值
        if (user.getRole() == null) user.setRole("USER");
        if (user.getStatus() == null) user.setStatus(1);
        if (user.getCreateTime() == null) user.setCreateTime(new Date());

        // 设置密码（如果命令中没有提供密码，则生成随机密码）
        String password = command.getPassword();
        if (password == null || password.isEmpty()) {
            password = UUID.randomUUID().toString().substring(0, 8);
        }
        user.setPassword(passwordEncoder.encode(password));

        try {
            userMapper.insert(user);
            // 清除缓存
            cacheService.clearAsync(CommonCacheConfig.USER_INFO_CACHE);
        } catch (DuplicateKeyException e) {
            String message = e.getMessage();
            if (message.contains("user.username")) {
                throw new BusinessException(409, "用户名已存在");
            } else if (message.contains("user.email")) {
                throw new BusinessException(409, "邮箱已注册");
            } else if (message.contains("idx_user_openid")) {
                throw new BusinessException(409, "该微信账号已绑定其他用户");
            } else {
                throw new BusinessException(409, "用户信息重复");
            }
        }

        return convertToDTO(user);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        int count = userMapper.updateById(user);
        if (count > 0) {
            // 清除缓存
            cacheService.clearAsync(CommonCacheConfig.USER_INFO_CACHE);
        }
        return count > 0;
    }

    @Override
    public UserInfoDTO updateUser(UserUpdateCommand command) throws BusinessException {
        if (command == null) {
            throw new BusinessException(400, "更新命令不能为空");
        }

        if (command.getId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 获取当前用户数据
        User currentUser = this.getById(command.getId());
        if (currentUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 创建更新实体，只更新允许的字段
        User updateUser = new User();
        updateUser.setId(command.getId());

        // 只复制非null字段，保留原有值
        if (command.getUsername() != null) updateUser.setUsername(command.getUsername());
        if (command.getEmail() != null) updateUser.setEmail(command.getEmail());
        if (command.getRole() != null) updateUser.setRole(command.getRole());
        if (command.getStatus() != null) updateUser.setStatus(command.getStatus());

        // 保留原有头像URL，除非明确要更新
        if (command.getAvatarUrl() != null) {
            updateUser.setAvatarUrl(command.getAvatarUrl());
        } else {
            updateUser.setAvatarUrl(currentUser.getAvatarUrl());
        }

        // 强制设置角色为普通用户（如果是管理员调用，可以在调用前设置不同的角色）
        updateUser.setRole("USER");

        boolean success = this.updateById(updateUser);
        if (!success) {
            throw new BusinessException(500, "更新失败");
        }

        // 清除缓存
        cacheService.clearAsync(CommonCacheConfig.USER_INFO_CACHE);

        User updatedUser = this.getById(command.getId());
        return convertToDTO(updatedUser);
    }

    @Override
    public UserInfoDTO getUserByOpenid(String openid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        User user = userMapper.selectOne(queryWrapper);
        return convertToDTO(user);
    }

    /**
     * 将User实体转换为UserInfoDTO
     */
    @Override
    public UserInfoDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserInfoDTO dto = new UserInfoDTO();
        // 使用Spring的BeanUtils复制属性
        BeanUtils.copyProperties(user, dto);

        return dto;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(Integer page, Integer size, Integer status, String keyword, String timeFilter) {
        StringBuilder keyBuilder = new StringBuilder("page:");
        keyBuilder.append("page_").append(page);
        keyBuilder.append("_size_").append(size);

        if (status != null) {
            keyBuilder.append("_status_").append(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyBuilder.append("_keyword_").append(keyword);
        }

        if (timeFilter != null && !timeFilter.trim().isEmpty()) {
            keyBuilder.append("_timeFilter_").append(timeFilter);
        }

        return keyBuilder.toString();
    }

    /**
     * 封装分页查询逻辑，返回UserInfoDTO对象
     */
    @Override
    public PageResult<UserInfoDTO> getUserInfoPage(Integer page, Integer size, Integer status, String keyword, String timeFilter) {
        // 构建缓存键
        String cacheKey = buildCacheKey(page, size, status, keyword, timeFilter);

        // 尝试从缓存获取
        PageResult<UserInfoDTO> cachedResult = cacheService.get(CommonCacheConfig.USER_INFO_CACHE, cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 缓存未命中，从数据库获取
        log.info("从数据库获取用户分页数据");

        // 创建分页对象
        Page<User> pageObj = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 关键词筛选：查询用户名或邮箱包含关键字
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("username", keyword)
                    .or()
                    .like("email", keyword));
        }

        // 时间筛选：根据用户注册时间 (字段：create_time)
        if (timeFilter != null && !timeFilter.trim().isEmpty()) {
            Date now = new Date();
            if ("最近一周".equals(timeFilter)) {
                queryWrapper.ge("create_time", new Date(now.getTime() - 7L * 24 * 60 * 60 * 1000));
            } else if ("最近一月".equals(timeFilter)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.MONTH, -1);
                queryWrapper.ge("create_time", cal.getTime());
            } else if ("最近三月".equals(timeFilter)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.MONTH, -3);
                queryWrapper.ge("create_time", cal.getTime());
            }
        }

        // 执行分页查询
        IPage<User> userPage = this.page(pageObj, queryWrapper);

        // 将User列表转换为UserInfoDTO列表
        List<UserInfoDTO> dtoList = userPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 创建UserInfoDTO的分页结果
        PageResult<UserInfoDTO> result = PageResult.of(dtoList, userPage.getTotal(), (int)userPage.getCurrent(), (int)userPage.getSize());

        // 处理用户头像URL
        result = processUserAvatars(result);

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.USER_INFO_CACHE, cacheKey, result, CACHE_EXPIRATION_MINUTES);

        return result;
    }

    /**
     * 处理用户头像URL，生成可访问的完整URL
     */
    private PageResult<UserInfoDTO> processUserAvatars(PageResult<UserInfoDTO> result) {
        List<UserInfoDTO> processedUsers = result.getRecords().stream()
                .map(user -> {
                    // 如果用户有头像URL，则生成可访问的完整URL
                    if (user != null && StringUtils.hasText(user.getAvatarUrl())) {
                        try {
                            // 生成有效期为30分钟的预签名URL
                            String fullAvatarUrl = fileService.generateDownloadPresignedUrl(user.getAvatarUrl(), 30);
                            user.setAvatarUrl(fullAvatarUrl);
                        } catch (Exception e) {
                            // 如果生成URL失败，记录错误但不影响其他用户数据返回
                            log.error("生成用户 " + user.getId() + " 的头像URL失败: " + e.getMessage(), e);
                            // 将avatarUrl设为空，前端会显示用户名首字母作为头像
                            user.setAvatarUrl("");
                        }
                    }
                    return user;
                })
                .collect(Collectors.toList());

        // 更新处理后的用户列表
        result.setRecords(processedUsers);
        return result;
    }

    /**
     * 更新用户头像URL
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatarUrl) throws BusinessException {
        if (userId == null || userId <= 0) {
            throw new BusinessException(400, "无效的用户ID");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 获取旧的头像路径
        String oldAvatarPath = user.getAvatarUrl();

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatarUrl(avatarUrl);

        int count = userMapper.updateById(updateUser);
        if (count <= 0) {
            throw new BusinessException(500, "更新头像失败");
        }

        // 清除缓存
        cacheService.clearAsync(CommonCacheConfig.USER_INFO_CACHE);

        // 如果存在旧头像，则删除
        if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
            try {
                // 直接使用数据库中保存的路径删除文件
                fileService.deleteFile(oldAvatarPath);
            } catch (Exception e) {
                // 删除旧头像失败不影响更新，只记录错误
                log.error("删除旧头像文件失败：" + e.getMessage(), e);
            }
        }

        return true;
    }

    /**
     * 验证用户密码
     */
    @Override
    public boolean verifyPassword(String usernameOrEmail, String password) {
        try {
            // 尝试使用用户名查找用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", usernameOrEmail)
                    .or()
                    .eq("email", usernameOrEmail);

            User user = userMapper.selectOne(queryWrapper);

            // 验证密码
            return user != null && passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            log.error("密码验证失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 修改用户密码
     */
    @Override
    public boolean changePassword(Long userId, String newPassword) throws BusinessException {
        if (userId == null || userId <= 0) {
            throw new BusinessException(400, "无效的用户ID");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException(400, "新密码不能为空");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(encodedPassword);

        int count = userMapper.updateById(updateUser);
        if (count <= 0) {
            throw new BusinessException(500, "密码更新失败");
        }

        // 清除缓存
        cacheService.clearAsync(CommonCacheConfig.USER_INFO_CACHE);

        return true;
    }
}
