package com.example.nutrition.service;

import com.example.diet.command.DietRecordQueryCommand;
import com.example.nutrition.command.NutritionAdviceCommand;
import com.example.nutrition.command.NutritionStatCommand;
import com.example.nutrition.command.NutritionTrendCommand;
import com.example.diet.dto.DietRecordFoodDTO;
import com.example.diet.dto.DietRecordResponseDTO;
import com.example.nutrition.dto.*;
import com.example.user.dto.UserNutritionGoalResponseDTO;
import com.example.shared.response.PageResult;
import com.example.diet.service.DietRecordService;
import com.example.nutrition.service.NutritionAdviceService;
import com.example.nutrition.service.NutritionStatService;
import com.example.user.service.UserNutritionGoalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 营养统计服务实现类
 */
@Slf4j
@Service
@DubboService
public class NutritionStatServiceImpl implements NutritionStatService {

    @DubboReference
    private DietRecordService dietRecordService;

    @DubboReference
    private UserNutritionGoalService userNutritionGoalService;

    @Autowired
    private NutritionAdviceService nutritionAdviceService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Cacheable(value = "nutritionStat", key = "'daily_' + #command.userId + '_' + #command.date")
    public NutritionStatDTO getDailyNutritionStat(NutritionStatCommand command) {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();

        // 查询用户营养目标
        UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoalService.getNutritionGoal(userId);

        // 构建查询命令获取当日饮食记录
        DietRecordQueryCommand queryCommand = new DietRecordQueryCommand();
        queryCommand.setUserId(userId);
        queryCommand.setStartDate(date.format(DATE_FORMATTER));
        queryCommand.setEndDate(date.format(DATE_FORMATTER));
        queryCommand.setPage(1);
        queryCommand.setSize(1000); // 设置足够大的页面大小获取当日所有记录

        PageResult<DietRecordResponseDTO> dietRecordsResult = dietRecordService.getDietRecords(queryCommand);

        // 初始化营养统计数据
        NutritionStatDTO nutritionStat = new NutritionStatDTO();
        nutritionStat.setDate(date.format(DATE_FORMATTER));
        nutritionStat.setCalorie(0);
        nutritionStat.setProtein(0.0);
        nutritionStat.setCarbs(0.0);
        nutritionStat.setFat(0.0);

        // 汇总当日营养数据
        for (DietRecordResponseDTO dietRecord : dietRecordsResult.getRecords()) {
            if (dietRecord.getFoods() != null) {
                for (DietRecordFoodDTO food : dietRecord.getFoods()) {
                    nutritionStat.setCalorie(nutritionStat.getCalorie() + (food.getCalories() != null ? food.getCalories().intValue() : 0));
                    nutritionStat.setProtein(nutritionStat.getProtein() + (food.getProtein() != null ? food.getProtein().doubleValue() : 0));
                    nutritionStat.setCarbs(nutritionStat.getCarbs() + (food.getCarbs() != null ? food.getCarbs().doubleValue() : 0));
                    nutritionStat.setFat(nutritionStat.getFat() + (food.getFat() != null ? food.getFat().doubleValue() : 0));
                }
            }
        }

        // 计算目标达成百分比
        if (nutritionGoal.getCalorieTarget() != null && nutritionGoal.getCalorieTarget() > 0) {
            nutritionStat.setCaloriePercentage(nutritionStat.getCalorie() * 100.0 / nutritionGoal.getCalorieTarget());
        } else {
            nutritionStat.setCaloriePercentage(0.0);
        }

        if (nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() > 0) {
            nutritionStat.setProteinPercentage(nutritionStat.getProtein() * 100.0 / nutritionGoal.getProteinTarget());
        } else {
            nutritionStat.setProteinPercentage(0.0);
        }

        if (nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() > 0) {
            nutritionStat.setCarbsPercentage(nutritionStat.getCarbs() * 100.0 / nutritionGoal.getCarbsTarget());
        } else {
            nutritionStat.setCarbsPercentage(0.0);
        }

        if (nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() > 0) {
            nutritionStat.setFatPercentage(nutritionStat.getFat() * 100.0 / nutritionGoal.getFatTarget());
        } else {
            nutritionStat.setFatPercentage(0.0);
        }

        return nutritionStat;
    }



    @Override
    @Cacheable(value = "nutritionStat", key = "'trend_' + #command.userId + '_' + #command.startDate + '_' + #command.endDate")
    public NutritionTrendDTO getNutritionTrend(NutritionTrendCommand command) {
        Long userId = command.getUserId();
        LocalDate startDate = command.getStartDate();
        LocalDate endDate = command.getEndDate();

        log.debug("计算用户营养趋势: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 一次性批量获取整个日期范围的饮食记录
        Map<Long, Map<String, List<DietRecordResponseDTO>>> batchDietRecords =
            dietRecordService.getBatchDietRecordsForNutritionStat(Arrays.asList(userId), startDate, endDate);

        // 获取用户的饮食记录
        Map<String, List<DietRecordResponseDTO>> userRecords = batchDietRecords.get(userId);
        if (userRecords == null) {
            userRecords = new HashMap<>();
        }

        // 只查询一次用户营养目标
        UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoalService.getNutritionGoal(userId);

        NutritionTrendDTO trendDTO = new NutritionTrendDTO();
        List<String> dateList = new ArrayList<>();
        List<Integer> calorieList = new ArrayList<>();
        List<Double> proteinList = new ArrayList<>();
        List<Double> carbsList = new ArrayList<>();
        List<Double> fatList = new ArrayList<>();

        // 遍历日期范围，计算每日营养数据
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            dateList.add(dateStr);

            // 获取当日饮食记录
            List<DietRecordResponseDTO> dayRecords = userRecords.get(dateStr);

            if (dayRecords != null && !dayRecords.isEmpty()) {
                // 计算当日营养摄入
                NutritionStatDTO dailyStat = calculateNutritionFromRecordsWithGoal(dayRecords, nutritionGoal, currentDate);
                calorieList.add(dailyStat.getCalorie());
                proteinList.add(dailyStat.getProtein());
                carbsList.add(dailyStat.getCarbs());
                fatList.add(dailyStat.getFat());
            } else {
                // 如果当天没有饮食记录，添加0值
                calorieList.add(0);
                proteinList.add(0.0);
                carbsList.add(0.0);
                fatList.add(0.0);
            }

            currentDate = currentDate.plusDays(1);
        }

        trendDTO.setDateList(dateList);
        trendDTO.setCalorieList(calorieList);
        trendDTO.setProteinList(proteinList);
        trendDTO.setCarbsList(carbsList);
        trendDTO.setFatList(fatList);

        log.debug("营养趋势计算完成: userId={}, 天数={}", userId, dateList.size());
        return trendDTO;
    }



    @Override
    @Cacheable(value = "nutritionStat", key = "'details_' + #command.userId + '_' + #command.date")
    public List<NutritionDetailItemDTO>getNutritionDetails(NutritionStatCommand command)  {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();

        // 获取当日营养摄入统计（已包含百分比计算）
        NutritionStatDTO nutritionStat = getDailyNutritionStat(NutritionStatCommand.of(userId, date));

        List<NutritionDetailItemDTO> detailList = new ArrayList<>();

        // 直接使用已计算的百分比，限制最大值为100%
        detailList.add(new NutritionDetailItemDTO("蛋白质", nutritionStat.getProtein(), "g",
            Math.min(100, nutritionStat.getProteinPercentage())));
        detailList.add(new NutritionDetailItemDTO("碳水化合物", nutritionStat.getCarbs(), "g",
            Math.min(100, nutritionStat.getCarbsPercentage())));
        detailList.add(new NutritionDetailItemDTO("脂肪", nutritionStat.getFat(), "g",
            Math.min(100, nutritionStat.getFatPercentage())));

        return detailList;
    }



    @Override
    @Cacheable(value = "nutritionStat", key = "'advice_' + #command.userId + '_' + #command.date")
    public List<NutritionAdviceDisplayDTO> getNutritionAdvice(NutritionAdviceCommand command) {
        Long userId = command.getUserId();
        LocalDate date = command.getDate();

        List<NutritionAdviceDisplayDTO> adviceList = new ArrayList<>();

        // 获取当日营养摄入统计
        NutritionStatDTO nutritionStat = getDailyNutritionStat(NutritionStatCommand.of(userId, date));

        // 根据与目标的差距生成建议

        // 检查蛋白质摄入
        NutritionAdviceResponseDTO proteinAdvice = nutritionAdviceService.getAdviceByCondition("protein", nutritionStat.getProteinPercentage().intValue());
        if (proteinAdvice != null) {
            adviceList.add(convertResponseDTOToDTO(proteinAdvice));
        }

        // 检查碳水化合物摄入
        NutritionAdviceResponseDTO carbsAdvice = nutritionAdviceService.getAdviceByCondition("carbs", nutritionStat.getCarbsPercentage().intValue());
        if (carbsAdvice != null) {
            adviceList.add(convertResponseDTOToDTO(carbsAdvice));
        }

        // 检查脂肪摄入
        NutritionAdviceResponseDTO fatAdvice = nutritionAdviceService.getAdviceByCondition("fat", nutritionStat.getFatPercentage().intValue());
        if (fatAdvice != null) {
            adviceList.add(convertResponseDTOToDTO(fatAdvice));
        }

        // 检查热量摄入
        NutritionAdviceResponseDTO calorieAdvice = nutritionAdviceService.getAdviceByCondition("calorie", nutritionStat.getCaloriePercentage().intValue());
        if (calorieAdvice != null) {
            adviceList.add(convertResponseDTOToDTO(calorieAdvice));
        }

        // 如果没有任何建议，添加一个默认建议
        if (adviceList.isEmpty()) {
            NutritionAdviceResponseDTO defaultAdvice = nutritionAdviceService.getDefaultAdvice();
            if (defaultAdvice != null) {
                adviceList.add(convertResponseDTOToDTO(defaultAdvice));
            } else {
                // 如果数据库中没有默认建议，使用硬编码的默认建议
                adviceList.add(new NutritionAdviceDisplayDTO(
                        "info",
                        "营养摄入基本合理",
                        "今日的营养摄入基本合理，保持均衡饮食有助于健康。"
                ));
            }
        }

        return adviceList;
    }

    @Override
    @Cacheable(value = "nutritionStat", key = "'compliance_' + #date")
    public double calculateNutritionComplianceRate(LocalDate date) {
        // 获取所有活跃用户ID列表
        List<Long> activeUserIds = dietRecordService.findActiveUserIdsByDate(date);

        if (activeUserIds.isEmpty()) {
            return 0.0; // 如果没有活跃用户，返回0
        }

        // 批量获取所有用户当日的饮食记录（一次RPC调用）
        Map<Long, Map<String, List<DietRecordResponseDTO>>> batchDietRecords =
            dietRecordService.getBatchDietRecordsForNutritionStat(activeUserIds, date, date);

        // 预先查询所有用户的营养目标并缓存（避免重复查询）
        Map<Long, UserNutritionGoalResponseDTO> userNutritionGoals = new HashMap<>();
        for (Long userId : activeUserIds) {
            try {
                UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoalService.getNutritionGoal(userId);
                if (nutritionGoal != null) {
                    userNutritionGoals.put(userId, nutritionGoal);
                }
            } catch (Exception e) {
                log.warn("获取用户营养目标失败: userId={}", userId, e);
            }
        }

        int compliantUsers = 0;
        String dateStr = date.format(DATE_FORMATTER);

        // 遍历所有活跃用户，检查他们的营养达标情况
        for (Long userId : activeUserIds) {
            try {
                // 从批量查询结果中获取用户当日的饮食记录
                Map<String, List<DietRecordResponseDTO>> userRecords = batchDietRecords.get(userId);
                if (userRecords != null) {
                    List<DietRecordResponseDTO> dayRecords = userRecords.get(dateStr);
                    if (dayRecords != null && !dayRecords.isEmpty()) {
                        // 使用缓存的营养目标计算当日营养摄入
                        UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoals.get(userId);
                        if (nutritionGoal != null) {
                            NutritionStatDTO nutritionStat = calculateNutritionFromRecordsWithGoal(dayRecords, nutritionGoal, date);

                            // 检查是否达标（这里简化为热量、蛋白质、碳水和脂肪都达到目标的80%以上）
                            boolean isCompliant =
                                nutritionStat.getCaloriePercentage() >= 80 &&
                                nutritionStat.getProteinPercentage() >= 80 &&
                                nutritionStat.getCarbsPercentage() >= 80 &&
                                nutritionStat.getFatPercentage() >= 80;

                            if (isCompliant) {
                                compliantUsers++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略单个用户的错误，继续处理其他用户
                log.error("计算用户营养达标率失败: userId={}, date={}", userId, date, e);
            }
        }

        // 计算达标率
        double complianceRate = (double) compliantUsers / activeUserIds.size() * 100;

        return complianceRate;
    }



    @Override
    @Cacheable(value = "nutritionStat", key = "'allTrend_' + #period")
    public Map<String, Object> getAllNutritionTrend(String period) {
        // 处理日期参数
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;

        // 根据period设置日期范围
        switch (period) {
            case "week":
                startDate = today.minus(6, ChronoUnit.DAYS); // 最近一周
                break;
            case "month":
                startDate = today.minus(29, ChronoUnit.DAYS); // 最近一个月
                break;
            case "year":
                startDate = today.minus(364, ChronoUnit.DAYS); // 最近一年
                break;
            default:
                startDate = today.minus(29, ChronoUnit.DAYS); // 默认一个月
        }

        // 获取所有活跃用户ID列表
        List<Long> activeUserIds = dietRecordService.findActiveUserIdsByDateRange(startDate, endDate);

        if (activeUserIds.isEmpty()) {
            // 如果没有活跃用户，返回空数据
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("dateList", new ArrayList<String>());
            emptyResult.put("calorieList", new ArrayList<Double>());
            emptyResult.put("proteinList", new ArrayList<Double>());
            emptyResult.put("carbsList", new ArrayList<Double>());
            emptyResult.put("fatList", new ArrayList<Double>());
            return emptyResult;
        }

        // 批量获取所有用户在指定日期范围内的饮食记录（一次RPC调用）
        Map<Long, Map<String, List<DietRecordResponseDTO>>> batchDietRecords =
            dietRecordService.getBatchDietRecordsForNutritionStat(activeUserIds, startDate, endDate);

        // 预先查询所有用户的营养目标并缓存（避免重复查询）
        Map<Long, UserNutritionGoalResponseDTO> userNutritionGoals = new HashMap<>();
        for (Long userId : activeUserIds) {
            try {
                UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoalService.getNutritionGoal(userId);
                if (nutritionGoal != null) {
                    userNutritionGoals.put(userId, nutritionGoal);
                }
            } catch (Exception e) {
                log.warn("获取用户营养目标失败: userId={}", userId, e);
            }
        }

        // 准备结果数据结构
        List<String> dateList = new ArrayList<>();
        List<Double> calorieList = new ArrayList<>();
        List<Double> proteinList = new ArrayList<>();
        List<Double> carbsList = new ArrayList<>();
        List<Double> fatList = new ArrayList<>();

        // 遍历日期范围
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 添加日期
            dateList.add(currentDate.format(DATE_FORMATTER));
            String dateStr = currentDate.format(DATE_FORMATTER);

            // 计算当日所有用户的平均营养摄入
            double totalCalorie = 0;
            double totalProtein = 0;
            double totalCarbs = 0;
            double totalFat = 0;
            int userCount = 0;

            for (Long userId : activeUserIds) {
                try {
                    // 从批量查询结果中获取用户当日的饮食记录
                    Map<String, List<DietRecordResponseDTO>> userRecords = batchDietRecords.get(userId);
                    if (userRecords != null) {
                        List<DietRecordResponseDTO> dayRecords = userRecords.get(dateStr);
                        if (dayRecords != null && !dayRecords.isEmpty()) {
                            // 使用缓存的营养目标计算当日营养摄入
                            UserNutritionGoalResponseDTO nutritionGoal = userNutritionGoals.get(userId);
                            if (nutritionGoal != null) {
                                NutritionStatDTO nutritionStat = calculateNutritionFromRecordsWithGoal(dayRecords, nutritionGoal, currentDate);

                                // 累加营养数据
                                totalCalorie += nutritionStat.getCalorie();
                                totalProtein += nutritionStat.getProtein();
                                totalCarbs += nutritionStat.getCarbs();
                                totalFat += nutritionStat.getFat();
                                userCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略单个用户的错误，继续处理其他用户
                    log.error("计算用户营养数据失败: userId={}, date={}", userId, currentDate, e);
                }
            }

            // 计算平均值
            if (userCount > 0) {
                calorieList.add(totalCalorie / userCount);
                proteinList.add(totalProtein / userCount);
                carbsList.add(totalCarbs / userCount);
                fatList.add(totalFat / userCount);
            } else {
                // 如果当天没有数据，添加0
                calorieList.add(0.0);
                proteinList.add(0.0);
                carbsList.add(0.0);
                fatList.add(0.0);
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("calorieList", calorieList);
        result.put("proteinList", proteinList);
        result.put("carbsList", carbsList);
        result.put("fatList", fatList);

        return result;
    }





    /**
     * 根据饮食记录列表和营养目标计算营养统计数据
     * 用于已经获取营养目标的场景，避免重复查询
     *
     * @param dayRecords 当日饮食记录列表
     * @param nutritionGoal 用户营养目标
     * @param date 日期
     * @return 营养统计数据
     */
    private NutritionStatDTO calculateNutritionFromRecordsWithGoal(List<DietRecordResponseDTO> dayRecords,
                                                                  UserNutritionGoalResponseDTO nutritionGoal,
                                                                  LocalDate date) {
        // 初始化营养统计数据
        NutritionStatDTO nutritionStat = new NutritionStatDTO();
        nutritionStat.setDate(date.format(DATE_FORMATTER));
        nutritionStat.setCalorie(0);
        nutritionStat.setProtein(0.0);
        nutritionStat.setCarbs(0.0);
        nutritionStat.setFat(0.0);

        // 汇总当日营养数据
        for (DietRecordResponseDTO dietRecord : dayRecords) {
            if (dietRecord.getFoods() != null) {
                for (DietRecordFoodDTO food : dietRecord.getFoods()) {
                    nutritionStat.setCalorie(nutritionStat.getCalorie() + (food.getCalories() != null ? food.getCalories().intValue() : 0));
                    nutritionStat.setProtein(nutritionStat.getProtein() + (food.getProtein() != null ? food.getProtein().doubleValue() : 0));
                    nutritionStat.setCarbs(nutritionStat.getCarbs() + (food.getCarbs() != null ? food.getCarbs().doubleValue() : 0));
                    nutritionStat.setFat(nutritionStat.getFat() + (food.getFat() != null ? food.getFat().doubleValue() : 0));
                }
            }
        }

        // 计算目标达成百分比
        if (nutritionGoal.getCalorieTarget() != null && nutritionGoal.getCalorieTarget() > 0) {
            nutritionStat.setCaloriePercentage(nutritionStat.getCalorie() * 100.0 / nutritionGoal.getCalorieTarget());
        } else {
            nutritionStat.setCaloriePercentage(0.0);
        }

        if (nutritionGoal.getProteinTarget() != null && nutritionGoal.getProteinTarget() > 0) {
            nutritionStat.setProteinPercentage(nutritionStat.getProtein() * 100.0 / nutritionGoal.getProteinTarget());
        } else {
            nutritionStat.setProteinPercentage(0.0);
        }

        if (nutritionGoal.getCarbsTarget() != null && nutritionGoal.getCarbsTarget() > 0) {
            nutritionStat.setCarbsPercentage(nutritionStat.getCarbs() * 100.0 / nutritionGoal.getCarbsTarget());
        } else {
            nutritionStat.setCarbsPercentage(0.0);
        }

        if (nutritionGoal.getFatTarget() != null && nutritionGoal.getFatTarget() > 0) {
            nutritionStat.setFatPercentage(nutritionStat.getFat() * 100.0 / nutritionGoal.getFatTarget());
        } else {
            nutritionStat.setFatPercentage(0.0);
        }

        return nutritionStat;
    }

    /**
     * 将NutritionAdviceResponseDTO转换为NutritionAdviceDTO
     * @param responseDTO 营养建议响应DTO
     * @return 营养建议DTO
     */
    private NutritionAdviceDisplayDTO convertResponseDTOToDTO(NutritionAdviceResponseDTO responseDTO) {
        if (responseDTO == null) {
            return null;
        }
        return new NutritionAdviceDisplayDTO(
                responseDTO.getType(),
                responseDTO.getTitle(),
                responseDTO.getDescription()
        );
    }
}