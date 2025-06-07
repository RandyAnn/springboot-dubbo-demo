package com.example.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.user.command.NutritionGoalCommand;
import com.example.user.dto.UserNutritionGoalResponseDTO;
import com.example.user.entity.UserNutritionGoal;
import com.example.shared.exception.BusinessException;
import com.example.user.service.UserNutritionGoalService;
import com.example.user.mapper.UserNutritionGoalMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

/**
 * 用户营养目标服务实现类
 */
@Service
@DubboService
public class UserNutritionGoalServiceImpl extends ServiceImpl<UserNutritionGoalMapper, UserNutritionGoal>
        implements UserNutritionGoalService {

    private static final Logger logger = LoggerFactory.getLogger(UserNutritionGoalServiceImpl.class);

    private final UserNutritionGoalMapper userNutritionGoalMapper;


    @Autowired
    public UserNutritionGoalServiceImpl(
            UserNutritionGoalMapper userNutritionGoalMapper) {
        this.userNutritionGoalMapper = userNutritionGoalMapper;
    }



    @Override
    public UserNutritionGoalResponseDTO getNutritionGoal(Long userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        QueryWrapper<UserNutritionGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserNutritionGoal nutritionGoal = userNutritionGoalMapper.selectOne(queryWrapper);

        return convertToResponseDTO(nutritionGoal);
    }

    /**
     * 将UserNutritionGoal实体转换为UserNutritionGoalResponseDTO
     */
    private UserNutritionGoalResponseDTO convertToResponseDTO(UserNutritionGoal nutritionGoal) {
        if (nutritionGoal == null) {
            return null;
        }

        UserNutritionGoalResponseDTO dto = new UserNutritionGoalResponseDTO();
        BeanUtils.copyProperties(nutritionGoal, dto);

        // 转换Date到LocalDateTime
        if (nutritionGoal.getCreatedAt() != null) {
            dto.setCreatedAt(nutritionGoal.getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        if (nutritionGoal.getUpdatedAt() != null) {
            dto.setUpdatedAt(nutritionGoal.getUpdatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }

        return dto;
    }



    @Override
    public boolean UpdateNutritionGoal(NutritionGoalCommand command) throws BusinessException {
        if (command == null) {
            throw new BusinessException(400, "营养目标命令对象不能为空");
        }

        if (command.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 创建更新包装器，直接根据userId更新，无需查询
        UpdateWrapper<UserNutritionGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", command.getUserId());

        // 只设置非null字段进行更新，并进行验证
        if (command.getCalorieTarget() != null) {
            if (command.getCalorieTarget() < 0) {
                throw new BusinessException(400, "热量目标不能小于0");
            }
            updateWrapper.set("calorie_target", command.getCalorieTarget());
        }
        if (command.getWeightTarget() != null) {
            if (command.getWeightTarget().doubleValue() <= 0) {
                throw new BusinessException(400, "体重目标必须大于0");
            }
            updateWrapper.set("weight_target", command.getWeightTarget());
        }
        if (command.getProteinTarget() != null) {
            if (command.getProteinTarget() < 0) {
                throw new BusinessException(400, "蛋白质目标不能小于0");
            }
            updateWrapper.set("protein_target", command.getProteinTarget());
        }
        if (command.getCarbsTarget() != null) {
            if (command.getCarbsTarget() < 0) {
                throw new BusinessException(400, "碳水化合物目标不能小于0");
            }
            updateWrapper.set("carbs_target", command.getCarbsTarget());
        }
        if (command.getFatTarget() != null) {
            if (command.getFatTarget() < 0) {
                throw new BusinessException(400, "脂肪目标不能小于0");
            }
            updateWrapper.set("fat_target", command.getFatTarget());
        }
        if (command.getIsVegetarian() != null) {
            updateWrapper.set("is_vegetarian", command.getIsVegetarian());
        }
        if (command.getIsLowCarb() != null) {
            updateWrapper.set("is_low_carb", command.getIsLowCarb());
        }
        if (command.getIsHighProtein() != null) {
            updateWrapper.set("is_high_protein", command.getIsHighProtein());
        }
        if (command.getIsGlutenFree() != null) {
            updateWrapper.set("is_gluten_free", command.getIsGlutenFree());
        }
        if (command.getIsLowSodium() != null) {
            updateWrapper.set("is_low_sodium", command.getIsLowSodium());
        }

        // 设置更新时间
        updateWrapper.set("updated_at", new Date());

        // 执行更新，返回影响的行数 > 0 表示成功
        int updatedRows = userNutritionGoalMapper.update(null, updateWrapper);

        if (updatedRows == 0) {
            throw new BusinessException(404, "用户营养目标不存在，请联系管理员");
        }

        return true;
    }


    /**
     * 创建默认营养目标并保存到数据库
     * @param userId 用户ID
     * @return 创建是否成功
     */
    @Override
    public boolean createDefaultNutritionGoal(Long userId) {
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

        // 保存到数据库
        return this.save(goal);
    }
}