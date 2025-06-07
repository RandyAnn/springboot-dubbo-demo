package com.example.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.food.entity.FoodCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 食物分类Mapper接口
 */
@Mapper
public interface FoodCategoryMapper extends BaseMapper<FoodCategory> {

    /**
     * 根据分类名称查询分类ID
     * @param name 分类名称
     * @return 分类ID
     */
    @Select("SELECT id FROM food_category WHERE name = #{name}")
    Integer selectIdByName(@Param("name") String name);

    /**
     * 统计分类下的食物数量
     * @param categoryId 分类ID
     * @return 食物数量
     */
    @Select("SELECT COUNT(*) FROM food WHERE category_id = #{categoryId}")
    Integer countFoodByCategoryId(@Param("categoryId") Integer categoryId);
}
