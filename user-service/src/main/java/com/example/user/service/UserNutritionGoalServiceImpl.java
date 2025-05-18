package com.example.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.command.NutritionGoalCommand;
import com.example.common.entity.UserNutritionGoal;
import com.example.common.exception.BusinessException;
import com.example.common.service.UserNutritionGoalService;
import com.example.user.mapper.UserNutritionGoalMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;

import java.util.Date;

/**
 * 用户营养目标服务实现类
 */
@Service
@DubboService
public class UserNutritionGoalServiceImpl extends ServiceImpl<UserNutritionGoalMapper, UserNutritionGoal>
        implements UserNutritionGoalService {

    private static final Logger logger = LoggerFactory.getLogger(UserNutritionGoalServiceImpl.class);

    /**
     * 用户营养目标缓存名称
     */
    public static final String USER_NUTRITION_GOAL_CACHE = CommonCacheConfig.USER_NUTRITION_GOAL_CACHE;

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRATION_MINUTES = 24 * 60; // 24小时

    private final UserNutritionGoalMapper userNutritionGoalMapper;
    private final CacheService cacheService;

    @Autowired
    public UserNutritionGoalServiceImpl(
            UserNutritionGoalMapper userNutritionGoalMapper,
            CacheService cacheService) {
        this.userNutritionGoalMapper = userNutritionGoalMapper;
        this.cacheService = cacheService;
    }



    @Override
    public UserNutritionGoal getNutritionGoalByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 尝试从缓存中获取
        UserNutritionGoal cachedGoal = cacheService.get(USER_NUTRITION_GOAL_CACHE, userId);
        if (cachedGoal != null) {
            return cachedGoal;
        }

        // 缓存未命中，从数据库查询
        QueryWrapper<UserNutritionGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserNutritionGoal nutritionGoal = userNutritionGoalMapper.selectOne(queryWrapper);

        // 将结果存入缓存
        if (nutritionGoal != null) {
            cacheService.putAsync(USER_NUTRITION_GOAL_CACHE, userId, nutritionGoal, CACHE_EXPIRATION_MINUTES);
        }

        return nutritionGoal;
    }



    @Override
    public UserNutritionGoal saveOrUpdateNutritionGoal(NutritionGoalCommand command) throws BusinessException {
        if (command == null) {
            throw new BusinessException(400, "营养目标命令对象不能为空");
        }

        if (command.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 检查是否存在已有记录
        UserNutritionGoal existingGoal = getNutritionGoalByUserId(command.getUserId());

        // 创建或更新营养目标实体
        UserNutritionGoal nutritionGoal;
        if (existingGoal != null) {
            nutritionGoal = existingGoal;
            // 只复制非null字段，保留原有值
            if (command.getCalorieTarget() != null) nutritionGoal.setCalorieTarget(command.getCalorieTarget());
            if (command.getWeightTarget() != null) nutritionGoal.setWeightTarget(command.getWeightTarget());
            if (command.getProteinTarget() != null) nutritionGoal.setProteinTarget(command.getProteinTarget());
            if (command.getCarbsTarget() != null) nutritionGoal.setCarbsTarget(command.getCarbsTarget());
            if (command.getFatTarget() != null) nutritionGoal.setFatTarget(command.getFatTarget());
            if (command.getIsVegetarian() != null) nutritionGoal.setIsVegetarian(command.getIsVegetarian());
            if (command.getIsLowCarb() != null) nutritionGoal.setIsLowCarb(command.getIsLowCarb());
            if (command.getIsHighProtein() != null) nutritionGoal.setIsHighProtein(command.getIsHighProtein());
            if (command.getIsGlutenFree() != null) nutritionGoal.setIsGlutenFree(command.getIsGlutenFree());
            if (command.getIsLowSodium() != null) nutritionGoal.setIsLowSodium(command.getIsLowSodium());
            nutritionGoal.setUpdatedAt(new Date());
        } else {
            nutritionGoal = new UserNutritionGoal();
            BeanUtils.copyProperties(command, nutritionGoal);
            nutritionGoal.setCreatedAt(new Date());
            nutritionGoal.setUpdatedAt(new Date());
        }

        // 验证营养目标数据
        validateNutritionGoal(nutritionGoal);

        // 保存或更新
        boolean success;
        if (existingGoal != null) {
            success = this.updateById(nutritionGoal);
        } else {
            success = this.save(nutritionGoal);
        }

        if (!success) {
            throw new BusinessException(500, "保存营养目标失败");
        }

        // 更新缓存
        cacheService.put(USER_NUTRITION_GOAL_CACHE, nutritionGoal.getUserId(), nutritionGoal, CACHE_EXPIRATION_MINUTES);

        return nutritionGoal;
    }

    @Override
    public UserNutritionGoal getOrCreateNutritionGoalByUserId(Long userId) {
        // 首先尝试获取用户现有的营养目标
        UserNutritionGoal nutritionGoal = getNutritionGoalByUserId(userId);

        // 如果不存在，创建默认营养目标
        if (nutritionGoal == null) {
            nutritionGoal = createDefaultNutritionGoal(userId);
            try {
                // 创建命令对象并保存
                NutritionGoalCommand command = new NutritionGoalCommand();
                command.setUserId(userId);
                command.setCalorieTarget(nutritionGoal.getCalorieTarget());
                command.setProteinTarget(nutritionGoal.getProteinTarget());
                command.setCarbsTarget(nutritionGoal.getCarbsTarget());
                command.setFatTarget(nutritionGoal.getFatTarget());
                command.setIsVegetarian(nutritionGoal.getIsVegetarian());
                command.setIsLowCarb(nutritionGoal.getIsLowCarb());
                command.setIsHighProtein(nutritionGoal.getIsHighProtein());
                command.setIsGlutenFree(nutritionGoal.getIsGlutenFree());
                command.setIsLowSodium(nutritionGoal.getIsLowSodium());
                command.setWeightTarget(nutritionGoal.getWeightTarget());

                nutritionGoal = saveOrUpdateNutritionGoal(command);
            } catch (BusinessException e) {
                logger.error("保存默认营养目标失败", e);
                // 即使保存失败，也返回默认目标对象（不返回null）
            }
        }

        return nutritionGoal;
    }

    /**
     * 创建默认营养目标
     * @param userId 用户ID
     * @return 默认营养目标
     */
    private UserNutritionGoal createDefaultNutritionGoal(Long userId) {
        UserNutritionGoal goal = new UserNutritionGoal();
        goal.setUserId(userId);
        goal.setCalorieTarget(2200);
        goal.setProteinTarget(65);
        goal.setCarbsTarget(300);
        goal.setFatTarget(70);
        goal.setIsVegetarian(false);
        goal.setIsLowCarb(false);
        goal.setIsHighProtein(false);
        goal.setIsGlutenFree(false);
        goal.setIsLowSodium(false);
        goal.setCreatedAt(new Date());
        goal.setUpdatedAt(new Date());
        return goal;
    }

    /**
     * 验证营养目标数据的合法性
     */
    private void validateNutritionGoal(UserNutritionGoal nutritionGoal) throws BusinessException {
        if (nutritionGoal == null) {
            throw new BusinessException(400, "营养目标数据不能为空");
        }

        if (nutritionGoal.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 验证热量目标
        if (nutritionGoal.getCalorieTarget() != null && nutritionGoal.getCalorieTarget() < 0) {
            throw new BusinessException(400, "热量目标不能小于0");
        }

        // 验证体重目标
        if (nutritionGoal.getWeightTarget() != null && nutritionGoal.getWeightTarget().doubleValue() <= 0) {
            throw new BusinessException(400, "体重目标必须大于0");
        }

        // 验证蛋白质目标
        if (nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() < 0) {
            throw new BusinessException(400, "蛋白质目标不能小于0");
        }

        // 验证碳水化合物目标
        if (nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() < 0) {
            throw new BusinessException(400, "碳水化合物目标不能小于0");
        }

        // 验证脂肪目标
        if (nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() < 0) {
            throw new BusinessException(400, "脂肪目标不能小于0");
        }
    }

    /**
     * 清除用户营养目标缓存
     */
    public void clearNutritionGoalCache(Long userId) {
        if (userId != null) {
            cacheService.evict(USER_NUTRITION_GOAL_CACHE, userId);
        }
    }
}