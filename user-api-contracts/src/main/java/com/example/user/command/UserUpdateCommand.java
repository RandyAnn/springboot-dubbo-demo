package com.example.user.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户信息更新命令对象
 */
@Data
public class UserUpdateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private String avatarUrl;
    private String password;
    private String openid;

    /**
     * 创建一个新的命令对象，设置用户ID
     *
     * @param userId 用户ID
     * @return 新的命令对象
     */
    public static UserUpdateCommand withUserId(Long userId) {
        UserUpdateCommand command = new UserUpdateCommand();
        command.setId(userId);
        return command;
    }
}
