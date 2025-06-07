package com.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.entity.UserNutritionGoal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户营养目标数据访问层
 */
@Mapper
public interface UserNutritionGoalMapper extends BaseMapper<UserNutritionGoal> {
}