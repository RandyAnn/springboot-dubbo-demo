package com.example.dietservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.command.diet.DietRecordAddCommand;
import com.example.common.command.diet.DietRecordDeleteCommand;
import com.example.common.command.diet.DietRecordQueryCommand;
import com.example.common.dto.diet.DietRecordFoodDTO;
import com.example.common.dto.diet.DietRecordResponseDTO;
import com.example.common.dto.user.UserInfoDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.example.common.entity.DietRecord;
import com.example.common.entity.DietRecordFood;
import com.example.common.event.events.DietRecordAddedEvent;
import com.example.common.event.EventPublisher;

import com.example.common.response.PageResult;
import com.example.common.service.DietRecordService;
import com.example.common.service.UserService;
import com.example.dietservice.mapper.DietRecordFoodMapper;
import com.example.dietservice.mapper.DietRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 饮食记录服务实现
 */
@Slf4j
@Service
@DubboService
public class DietRecordServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietRecordService {

    private final DietRecordMapper dietRecordMapper;
    private final DietRecordFoodMapper dietRecordFoodMapper;
    private final EventPublisher eventPublisher;

    @DubboReference
    private UserService userService;

    @Autowired
    public DietRecordServiceImpl(DietRecordMapper dietRecordMapper,
                                 DietRecordFoodMapper dietRecordFoodMapper,
                                 EventPublisher eventPublisher) {
        this.dietRecordMapper = dietRecordMapper;
        this.dietRecordFoodMapper = dietRecordFoodMapper;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Cacheable(value = "dietRecord", key = "'detail_' + #recordId")
    public DietRecordResponseDTO getDietRecordDetail(Long recordId) {
        log.debug("从数据库获取饮食记录详情: recordId={}", recordId);

        // 查询记录
        DietRecord dietRecord = dietRecordMapper.selectById(recordId);
        if (dietRecord == null) {
            return null;
        }

        return convertToResponseDTO(dietRecord);
    }


    /**
     * 将DietRecord转换为ResponseDTO
     */
    private DietRecordResponseDTO convertToResponseDTO(DietRecord dietRecord) {
        DietRecordResponseDTO responseDTO = new DietRecordResponseDTO();
        BeanUtils.copyProperties(dietRecord, responseDTO);

        // 获取用户信息
        try {
            UserInfoDTO user = userService.getUserById(dietRecord.getUserId());
            if (user != null) {
                responseDTO.setUsername(user.getUsername());
            } else {
                // 如果找不到用户，设置默认值
                responseDTO.setUsername("未知用户");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败，用户ID: {}", dietRecord.getUserId(), e);
            responseDTO.setUsername("未知用户");
        }

        // 获取食物明细
        LambdaQueryWrapper<DietRecordFood> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.eq(DietRecordFood::getDietRecordId, dietRecord.getId());
        List<DietRecordFood> foodList = dietRecordFoodMapper.selectList(foodWrapper);

        // 转换食物明细
        List<DietRecordFoodDTO> foodDTOList = foodList.stream().map(food -> {
            DietRecordFoodDTO foodDTO = new DietRecordFoodDTO();
            foodDTO.setFoodId(food.getFoodId());
            foodDTO.setName(food.getFoodName());
            foodDTO.setAmount(food.getAmount());
            foodDTO.setUnit(food.getUnit());
            foodDTO.setCalories(food.getCalories());
            foodDTO.setProtein(food.getProtein());
            foodDTO.setFat(food.getFat());
            foodDTO.setCarbs(food.getCarbs());
            foodDTO.setGrams(food.getGrams());
            return foodDTO;
        }).collect(Collectors.toList());

        responseDTO.setFoods(foodDTOList);

        return responseDTO;
    }

    @Override
    @Cacheable(value = "dietRecord", key = "'countByDate_' + #date")
    public int countDietRecordsByDate(LocalDate date) {
        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getDate, date);

        // 统计记录数
        return Math.toIntExact(dietRecordMapper.selectCount(wrapper));
    }

    @Override
    @Cacheable(value = "dietRecord", key = "'activeUsers_' + #date")
    public List<Long> findActiveUserIdsByDate(LocalDate date) {
        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getDate, date)
                .select(DietRecord::getUserId)
                .groupBy(DietRecord::getUserId);

        // 查询并提取用户ID
        return dietRecordMapper.selectList(wrapper)
                .stream()
                .map(DietRecord::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "dietRecord", key = "'activeUsersRange_' + #startDate + '_' + #endDate")
    public List<Long> findActiveUserIdsByDateRange(LocalDate startDate, LocalDate endDate) {
        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(DietRecord::getDate, startDate, endDate)
                .select(DietRecord::getUserId)
                .groupBy(DietRecord::getUserId);

        // 查询并提取用户ID
        return dietRecordMapper.selectList(wrapper)
                .stream()
                .map(DietRecord::getUserId)
                .collect(Collectors.toList());
    }




    @Override
    @Cacheable(value = "dietRecord", key = "'popular_' + #period + '_' + #limit")
    public List<Map<String, Object>> getPopularFoodsByPeriod(String period, int limit) {
        // 根据时间周期确定日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "week":
                startDate = endDate.minusWeeks(1);
                break;
            case "quarter":
                startDate = endDate.minusMonths(3);
                break;
            case "month":
            default:
                startDate = endDate.minusMonths(1);
                break;
        }

        log.debug("从数据库查询热门食物数据, 日期范围: {} 至 {}, 周期: {}", startDate, endDate, period);
        // 从食物明细表查询该时间段内的热门食物
        List<Map<String, Object>> popularFoods = dietRecordMapper.findPopularFoods(startDate, endDate, limit);

        if (popularFoods == null) {
            return new ArrayList<>();
        }

        // 处理返回结果，确保符合前端期望的格式
        return popularFoods.stream().map(food -> {
            Map<String, Object> foodMap = new HashMap<>();
            foodMap.put("name", food.get("food_name"));
            foodMap.put("count", food.get("count"));
            return foodMap;
        }).collect(Collectors.toList());
    }



    @Override
    @Transactional
    @CacheEvict(value = "dietRecord", allEntries = true)
    public Long addDietRecord(DietRecordAddCommand command) {
        // 1. 保存饮食记录主表
        DietRecord dietRecord = new DietRecord();
        dietRecord.setUserId(command.getUserId());
        dietRecord.setDate(LocalDate.parse(command.getDate()));
        dietRecord.setTime(LocalTime.parse(command.getTime()));
        dietRecord.setMealType(command.getMealType());
        dietRecord.setRemark(command.getRemark());
        dietRecord.setTotalCalorie(command.getTotalCalorie());
        dietRecord.setCreatedAt(LocalDateTime.now());
        dietRecord.setUpdatedAt(LocalDateTime.now());

        dietRecordMapper.insert(dietRecord);
        Long recordId = dietRecord.getId();

        // 2. 保存食物明细
        if (command.getFoods() != null && !command.getFoods().isEmpty()) {
            List<DietRecordFood> foodList = new ArrayList<>(command.getFoods().size());

            for (DietRecordAddCommand.DietRecordFoodCommand foodCommand : command.getFoods()) {
                DietRecordFood food = new DietRecordFood();
                food.setDietRecordId(recordId);
                food.setFoodId(foodCommand.getFoodId());
                food.setFoodName(foodCommand.getName());
                food.setAmount(foodCommand.getAmount());
                food.setUnit(foodCommand.getUnit());
                food.setCalories(foodCommand.getCalories());
                food.setProtein(foodCommand.getProtein());
                food.setFat(foodCommand.getFat());
                food.setCarbs(foodCommand.getCarbs());
                food.setGrams(foodCommand.getGrams());
                food.setCreatedAt(LocalDateTime.now());

                foodList.add(food);
            }

            // 批量插入食物记录
            for (DietRecordFood food : foodList) {
                dietRecordFoodMapper.insert(food);
            }
        }

        // 3. 发布饮食记录添加事件
        try {
            DietRecordAddedEvent event = new DietRecordAddedEvent(
                command.getUserId(),
                recordId,
                dietRecord.getDate(),
                command.getMealType()
            );
            eventPublisher.publish(event);
            log.info("发布饮食记录添加事件: userId={}, recordId={}, date={}",
                command.getUserId(), recordId, dietRecord.getDate());
        } catch (Exception e) {
            log.error("发布饮食记录添加事件失败: userId={}, recordId={}",
                command.getUserId(), recordId, e);
            // 事件发布失败不影响主业务流程
        }

        return recordId;
    }

    @Override
    @Cacheable(value = "dietRecord", key = "'records_' + #command.userId + '_' + #command.page + '_' + #command.size + '_' + (#command.startDate ?: '') + '_' + (#command.endDate ?: '') + '_' + (#command.mealType ?: '')")
    public PageResult<DietRecordResponseDTO> getDietRecords(DietRecordQueryCommand command) {
        log.debug("从数据库查询饮食记录列表");

        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getUserId, command.getUserId());

        // 日期范围过滤
        if (StringUtils.isNotBlank(command.getStartDate())) {
            LocalDate startDate = LocalDate.parse(command.getStartDate());
            wrapper.ge(DietRecord::getDate, startDate);
        }
        if (StringUtils.isNotBlank(command.getEndDate())) {
            LocalDate endDate = LocalDate.parse(command.getEndDate());
            wrapper.le(DietRecord::getDate, endDate);
        }

        // 餐次类型过滤
        if (StringUtils.isNotBlank(command.getMealType())) {
            wrapper.eq(DietRecord::getMealType, command.getMealType());
        }

        // 按日期和时间倒序排列
        wrapper.orderByDesc(DietRecord::getDate).orderByDesc(DietRecord::getTime);

        // 执行分页查询
        IPage<DietRecord> page = new Page<>(command.getPage(), command.getSize());
        page = dietRecordMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        List<DietRecordResponseDTO> records = page.getRecords().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return PageResult.of(records, page.getTotal(), command.getPage(), command.getSize());
    }

    @Override
    public Map<Long, Map<String, List<DietRecordResponseDTO>>> getBatchDietRecordsForNutritionStat(
            List<Long> userIds, LocalDate startDate, LocalDate endDate) {

        log.debug("批量查询饮食记录用于营养统计: userIds={}, startDate={}, endDate={}",
                userIds.size(), startDate, endDate);

        if (userIds.isEmpty()) {
            return new HashMap<>();
        }

        // 构建查询条件：查询指定用户在指定日期范围内的所有饮食记录
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DietRecord::getUserId, userIds)
               .between(DietRecord::getDate, startDate, endDate)
               .orderByDesc(DietRecord::getDate)
               .orderByDesc(DietRecord::getTime);

        // 查询所有符合条件的饮食记录
        List<DietRecord> dietRecords = dietRecordMapper.selectList(wrapper);

        // 按用户ID和日期分组
        Map<Long, Map<String, List<DietRecordResponseDTO>>> result = new HashMap<>();

        for (DietRecord dietRecord : dietRecords) {
            Long userId = dietRecord.getUserId();
            String dateStr = dietRecord.getDate().toString();

            // 转换为ResponseDTO
            DietRecordResponseDTO responseDTO = convertToResponseDTO(dietRecord);

            // 按用户ID分组
            result.computeIfAbsent(userId, k -> new HashMap<>())
                  .computeIfAbsent(dateStr, k -> new ArrayList<>())
                  .add(responseDTO);
        }

        log.debug("批量查询完成，返回{}个用户的饮食记录", result.size());
        return result;
    }

    @Override
    @Cacheable(value = "dietRecord", key = "'all_' + #command.page + '_' + #command.size + '_' + (#command.startDate ?: '') + '_' + (#command.endDate ?: '') + '_' + (#command.mealType ?: '')")
    public PageResult<DietRecordResponseDTO> getAllUsersDietRecords(DietRecordQueryCommand command) {

        log.debug("管理员查询所有用户的饮食记录: userId={}, page={}, size={}, startDate={}, endDate={}, mealType={}",
                command.getUserId(), command.getPage(), command.getSize(),
                command.getStartDate(), command.getEndDate(), command.getMealType());

        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤，仅管理员后台使用
        if (command.getUserId() != null) {
            wrapper.eq(DietRecord::getUserId, command.getUserId());
        }

        // 日期范围过滤
        if (StringUtils.isNotBlank(command.getStartDate())) {
            LocalDate startDate = LocalDate.parse(command.getStartDate());
            wrapper.ge(DietRecord::getDate, startDate);
        }
        if (StringUtils.isNotBlank(command.getEndDate())) {
            LocalDate endDate = LocalDate.parse(command.getEndDate());
            wrapper.le(DietRecord::getDate, endDate);
        }

        // 餐次类型过滤
        if (StringUtils.isNotBlank(command.getMealType())) {
            wrapper.eq(DietRecord::getMealType, command.getMealType());
        }

        // 按日期和时间倒序排列，确保最新记录在前
        wrapper.orderByDesc(DietRecord::getDate).orderByDesc(DietRecord::getTime);

        // 执行分页查询
        IPage<DietRecord> page = new Page<>(command.getPage(), command.getSize());
        page = dietRecordMapper.selectPage(page, wrapper);

        // 转换为响应DTO
        List<DietRecordResponseDTO> records = page.getRecords().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        PageResult<DietRecordResponseDTO> result = PageResult.of(records, page.getTotal(), command.getPage(), command.getSize());
        log.debug("从数据库获取管理员查询的饮食记录列表, 共{}条记录", records.size());

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "dietRecord", allEntries = true)
    public boolean deleteDietRecord(DietRecordDeleteCommand command) {
        // 验证记录是否属于该用户
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();

        // 如果用户ID为null，则是管理员操作，不需要验证用户ID
        if (command.getUserId() != null) {
            wrapper.eq(DietRecord::getId, command.getRecordId()).eq(DietRecord::getUserId, command.getUserId());
        } else {
            wrapper.eq(DietRecord::getId, command.getRecordId());
        }

        DietRecord dietRecord = dietRecordMapper.selectOne(wrapper);
        if (dietRecord == null) {
            return false;
        }

        // 删除食物记录
        LambdaQueryWrapper<DietRecordFood> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.eq(DietRecordFood::getDietRecordId, command.getRecordId());
        dietRecordFoodMapper.delete(foodWrapper);

        // 删除主记录
        dietRecordMapper.deleteById(command.getRecordId());

        return true;
    }
}