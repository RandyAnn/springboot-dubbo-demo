package com.example.nutrition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.nutrition.entity.NutritionAdvice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营养建议数据访问接口
 */
@Mapper
public interface NutritionAdviceMapper extends BaseMapper<NutritionAdvice> {

    /**
     * 根据条件类型和百分比查询适用的营养建议
     * @param conditionType 条件类型
     * @param percentage 百分比值
     * @return 营养建议列表
     */
    @Select("SELECT * FROM nutrition_advice WHERE condition_type = #{conditionType} " +
            "AND min_percentage <= #{percentage} AND max_percentage >= #{percentage} " +
            "AND status = 1 ORDER BY priority DESC")
    List<NutritionAdvice> findByConditionTypeAndPercentage(
            @Param("conditionType") String conditionType,
            @Param("percentage") Integer percentage);

    /**
     * 获取默认建议
     * @return 默认营养建议
     */
    @Select("SELECT * FROM nutrition_advice WHERE is_default = 1 AND status = 1 " +
            "ORDER BY priority DESC LIMIT 1")
    NutritionAdvice findDefaultAdvice();

    /**
     * 根据条件类型查询所有启用的营养建议
     * @param conditionType 条件类型
     * @return 营养建议列表
     */
    @Select("SELECT * FROM nutrition_advice WHERE condition_type = #{conditionType} " +
            "AND status = 1 ORDER BY priority DESC")
    List<NutritionAdvice> findByConditionType(@Param("conditionType") String conditionType);
}
