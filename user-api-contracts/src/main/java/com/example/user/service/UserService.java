package com.example.user.service;

import com.example.user.command.UserCreateCommand;
import com.example.user.command.UserPageQueryCommand;
import com.example.user.command.UserUpdateCommand;
import com.example.user.dto.AvatarResponseDTO;
import com.example.user.dto.UserInfoDTO;
import com.example.shared.exception.BusinessException;
import com.example.shared.response.PageResult;

public interface UserService {

    /**
     * 根据ID获取用户信息
     */
    UserInfoDTO getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     */
    UserInfoDTO getUserByUsername(String username);

    /**
     * 根据邮箱获取用户信息
     */
    UserInfoDTO getUserByEmail(String email);

    /**
     * 根据微信openid获取用户信息
     */
    UserInfoDTO getUserByOpenid(String openid);

    /**
     * 使用命令对象创建新用户
     *
     * @param command 用户创建命令对象
     * @return 创建后的用户信息DTO
     * @throws BusinessException 创建失败时抛出业务异常
     */
    UserInfoDTO createUser(UserCreateCommand command) throws BusinessException;

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status) throws BusinessException;

    /**
     * 使用命令对象更新用户信息
     *
     * @param command 用户更新命令对象
     * @return 更新是否成功
     * @throws BusinessException 参数错误时抛出业务异常
     */
    boolean updateUser(UserUpdateCommand command) throws BusinessException;

    /**
     * 分页查询方法，返回UserInfoDTO对象，只包含前端需要的字段
     */
    PageResult<UserInfoDTO> getUserInfoPage(UserPageQueryCommand command);

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    long getTotalUserCount();

    /**
     * 更新用户头像URL
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 是否更新成功
     * @throws BusinessException 更新失败时抛出业务异常
     */
    boolean updateUserAvatar(Long userId, String avatarUrl) throws BusinessException;

    /**
     * 验证用户密码
     *
     * @param usernameOrEmail 用户名或邮箱
     * @param password 密码
     * @return 验证成功返回true，否则返回false
     */
    boolean verifyPassword(String usernameOrEmail, String password);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否修改成功
     * @throws BusinessException 修改失败时抛出业务异常
     */
    boolean changePassword(Long userId, String newPassword) throws BusinessException;

    /**
     * 生成用户头像上传URL
     *
     * @param userId 用户ID
     * @param contentType 文件类型
     * @return 头像响应DTO
     * @throws BusinessException 生成失败时抛出业务异常
     */
    AvatarResponseDTO generateAvatarUploadUrl(Long userId, String contentType) throws BusinessException;

    /**
     * 生成用户头像下载URL
     *
     * @param userId 用户ID
     * @return 头像响应DTO
     * @throws BusinessException 生成失败时抛出业务异常
     */
    AvatarResponseDTO generateAvatarDownloadUrl(Long userId) throws BusinessException;
}
