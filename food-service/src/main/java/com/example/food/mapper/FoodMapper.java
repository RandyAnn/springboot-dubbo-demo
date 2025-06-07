package com.example.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.food.entity.Food;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食物Mapper接口
 */
@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}