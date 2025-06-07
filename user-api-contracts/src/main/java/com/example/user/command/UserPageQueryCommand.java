package com.example.user.command;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户分页查询命令对象
 */
@Data
public class UserPageQueryCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;
    private Integer size;
    private Integer status;
    private String keyword;
    private String timeFilter;
}
