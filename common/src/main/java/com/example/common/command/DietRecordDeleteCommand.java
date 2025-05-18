package com.example.common.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食记录删除命令对象
 */
@Data
public class DietRecordDeleteCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 记录ID
     */
    private Long recordId;
    
    /**
     * 创建一个新的命令对象，设置用户ID和记录ID
     *
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 新的命令对象
     */
    public static DietRecordDeleteCommand of(Long userId, Long recordId) {
        DietRecordDeleteCommand command = new DietRecordDeleteCommand();
        command.setUserId(userId);
        command.setRecordId(recordId);
        return command;
    }
}
