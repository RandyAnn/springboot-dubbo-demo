package com.example.user.service;

import com.example.user.command.NutritionGoalCommand;
import com.example.user.dto.UserNutritionGoalResponseDTO;
import com.example.shared.exception.BusinessException;

/**
 * 用户营养目标服务接口
 */
public interface UserNutritionGoalService {

    /**
     * 根据用户ID获取营养目标
     *
     * @param userId 用户ID
     * @return 用户营养目标DTO，如果不存在返回null
     */
    UserNutritionGoalResponseDTO getNutritionGoal(Long userId);

    /**
     * 更新用户营养目标（使用命令对象）
     * 注意：假设用户在注册时已经创建了默认营养目标，因此不需要检查是否存在
     *
     * @param command 营养目标命令对象
     * @return 更新是否成功
     * @throws BusinessException 业务异常
     */
    boolean UpdateNutritionGoal(NutritionGoalCommand command) throws BusinessException;

    /**
     * 创建默认营养目标并保存到数据库
     *
     * @param userId 用户ID
     * @return 创建是否成功
     */
    boolean createDefaultNutritionGoal(Long userId);
}
