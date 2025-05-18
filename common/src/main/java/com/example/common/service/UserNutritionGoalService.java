package com.example.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.command.NutritionGoalCommand;
import com.example.common.entity.UserNutritionGoal;
import com.example.common.exception.BusinessException;

/**
 * 用户营养目标服务接口
 */
public interface UserNutritionGoalService extends IService<UserNutritionGoal> {

    /**
     * 根据用户ID获取营养目标
     *
     * @param userId 用户ID
     * @return 用户营养目标，如果不存在返回null
     */
    UserNutritionGoal getNutritionGoalByUserId(Long userId);



    /**
     * 保存或更新用户营养目标（使用命令对象）
     *
     * @param command 营养目标命令对象
     * @return 保存后的用户营养目标
     * @throws BusinessException 业务异常
     */
    UserNutritionGoal saveOrUpdateNutritionGoal(NutritionGoalCommand command) throws BusinessException;

    /**
     * 获取营养目标，如果不存在则创建默认营养目标
     *
     * @param userId 用户ID
     * @return 用户营养目标，不会返回null
     */
    UserNutritionGoal getOrCreateNutritionGoalByUserId(Long userId);
}