package com.example.common.service;

import com.example.common.command.DietRecordAddCommand;
import com.example.common.command.DietRecordDeleteCommand;
import com.example.common.command.DietRecordQueryCommand;
import com.example.common.dto.DietRecordDTO;
import com.example.common.dto.DietRecordQueryDTO;
import com.example.common.dto.DietRecordResponseDTO;
import com.example.common.response.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 饮食记录服务接口
 */
public interface DietRecordService {

    /**
     * 获取单条饮食记录详情
     * @param recordId 记录ID
     * @return 饮食记录响应DTO
     */
    DietRecordResponseDTO getDietRecordDetail(Long recordId);

    /**
     * 统计指定日期的饮食记录数量
     * @param date 日期
     * @return 记录数量
     */
    int countDietRecordsByDate(LocalDate date);

    /**
     * 获取指定日期有饮食记录的活跃用户ID列表
     * @param date 日期
     * @return 用户ID列表
     */
    List<Long> findActiveUserIdsByDate(LocalDate date);

    /**
     * 获取指定日期范围内有饮食记录的活跃用户ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户ID列表
     */
    List<Long> findActiveUserIdsByDateRange(LocalDate startDate, LocalDate endDate);







    /**
     * 根据时间周期获取热门食物统计
     * @param period 时间周期：week(周)、month(月)、quarter(季度)
     * @param limit 返回数量限制
     * @return 热门食物统计列表
     */
    List<Map<String, Object>> getPopularFoodsByPeriod(String period, int limit);

    /**
     * 添加饮食记录 - 使用Command对象
     * @param command 饮食记录添加命令对象
     * @return 记录ID
     */
    Long addDietRecord(DietRecordAddCommand command);

    /**
     * 获取用户饮食记录列表 - 使用Command对象
     * @param command 饮食记录查询命令对象
     * @return 分页结果
     */
    PageResult<DietRecordResponseDTO> getDietRecords(DietRecordQueryCommand command);

    /**
     * 获取所有用户的饮食记录列表（管理员使用）- 使用Command对象
     * @param command 饮食记录查询命令对象
     * @return 分页结果
     */
    PageResult<DietRecordResponseDTO> getAllUsersDietRecords(DietRecordQueryCommand command);

    /**
     * 删除饮食记录 - 使用Command对象
     * @param command 饮食记录删除命令对象
     * @return 是否成功
     */
    boolean deleteDietRecord(DietRecordDeleteCommand command);
}