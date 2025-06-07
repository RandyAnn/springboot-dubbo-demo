package com.example.diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.diet.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
    /**
     * 查询指定时间范围内的热门食物
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回数量限制
     * @return 热门食物统计列表
     */
    @Select("SELECT drf.food_name, COUNT(*) as count " +
            "FROM diet_record_foods drf " +
            "JOIN diet_records dr ON drf.diet_record_id = dr.id " +
            "WHERE dr.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY drf.food_name " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> findPopularFoods(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("limit") int limit);
}