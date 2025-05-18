package com.example.nutrition.mapper;

import com.example.common.entity.DietRecord;
import com.example.common.entity.DietRecordFood;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养统计数据访问接口
 */
@Mapper
public interface NutritionStatMapper {

    /**
     * 根据用户ID和日期查询饮食记录
     * @param userId 用户ID
     * @param date 日期
     * @return 饮食记录列表
     */
    @Select("SELECT * FROM diet_records WHERE user_id = #{userId} AND date = #{date}")
    List<DietRecord> findDietRecordsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 根据记录ID查询饮食记录中的食物
     * @param recordId 饮食记录ID
     * @return 食物列表
     */
    @Select("SELECT * FROM diet_record_foods WHERE diet_record_id = #{recordId}")
    List<DietRecordFood> findDietRecordFoodsByRecordId(@Param("recordId") Long recordId);

    /**
     * 根据用户ID和日期范围查询饮食记录
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 饮食记录列表
     */
    @Select("SELECT * FROM diet_records WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate}")
    List<DietRecord> findDietRecordsByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}