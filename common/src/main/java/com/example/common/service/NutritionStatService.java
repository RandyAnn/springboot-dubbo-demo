package com.example.common.service;

import com.example.common.command.NutritionAdviceCommand;
import com.example.common.command.NutritionStatCommand;
import com.example.common.command.NutritionTrendCommand;
import com.example.common.dto.NutritionAdviceDTO;
import com.example.common.dto.NutritionDetailItemDTO;
import com.example.common.dto.NutritionStatDTO;
import com.example.common.dto.NutritionTrendDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 营养统计服务接口
 */
public interface NutritionStatService {

    /**
     * 获取用户日营养统计
     * @param command 营养统计命令对象
     * @return 营养统计数据
     */
    NutritionStatDTO getDailyNutritionStat(NutritionStatCommand command);

    /**
     * 获取用户营养趋势
     * @param command 营养趋势命令对象
     * @return 营养趋势数据
     */
    NutritionTrendDTO getNutritionTrend(NutritionTrendCommand command);

    /**
     * 获取用户营养摄入详情
     * @param command 营养统计命令对象
     * @return 营养摄入详情列表
     */
    List<NutritionDetailItemDTO> getNutritionDetails(NutritionStatCommand command);

    /**
     * 获取用户营养建议
     * @param command 营养建议命令对象
     * @return 营养建议列表
     */
    List<NutritionAdviceDTO> getNutritionAdvice(NutritionAdviceCommand command);

    /**
     * 计算指定日期的营养达标率
     * 营养达标率是指当天所有用户中，营养摄入达到目标的用户比例
     * @param date 日期
     * @return 营养达标率（百分比，0-100）
     */
    double calculateNutritionComplianceRate(LocalDate date);

    /**
     * 获取管理员仪表盘的用户营养摄入趋势数据
     * 该方法返回所有用户的平均营养摄入趋势
     *
     * @param period 时间周期：week(周)、month(月)、year(年)
     * @param startDate 开始日期，如果为null则根据period自动计算
     * @param endDate 结束日期，如果为null则使用当前日期
     * @return 包含趋势数据的Map，包括dateList、calorieList、proteinList、carbsList、fatList
     */
    Map<String, Object> getAdminNutritionTrend(String period, LocalDate startDate, LocalDate endDate);
}