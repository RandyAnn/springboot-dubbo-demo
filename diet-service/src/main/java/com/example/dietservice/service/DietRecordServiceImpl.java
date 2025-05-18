package com.example.dietservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.command.DietRecordAddCommand;
import com.example.common.command.DietRecordDeleteCommand;
import com.example.common.command.DietRecordQueryCommand;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.*;
import com.example.common.entity.DietRecord;
import com.example.common.entity.DietRecordFood;
import com.example.common.event.cache.CacheEventPublisher;
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
import java.time.format.DateTimeFormatter;
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
    private final CacheService cacheService;
    private final CacheEventPublisher cacheEventPublisher;

    @DubboReference
    private UserService userService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long CACHE_EXPIRATION_MINUTES = 24 * 60; // 缓存24小时
    private static final long POPULAR_FOODS_CACHE_TTL = 30; // 缓存30分钟

    @Autowired
    public DietRecordServiceImpl(DietRecordMapper dietRecordMapper,
                                 DietRecordFoodMapper dietRecordFoodMapper,
                                 CacheService cacheService,
                                 CacheEventPublisher cacheEventPublisher) {
        this.dietRecordMapper = dietRecordMapper;
        this.dietRecordFoodMapper = dietRecordFoodMapper;
        this.cacheService = cacheService;
        this.cacheEventPublisher = cacheEventPublisher;
    }

    /**
     * 清除与用户饮食记录相关的缓存
     *
     * @param userId  用户ID
     * @param dateStr 日期字符串，格式：yyyy-MM-dd
     */
    private void clearRelatedCaches(Long userId, String dateStr) {
        try {
            // 1. 清除饮食记录缓存
            cacheService.clear(CommonCacheConfig.DIET_RECORD_CACHE);

            // 2. 清除饮食统计缓存
            // 清除用户的活跃用户缓存
            if (dateStr != null) {
                // 清除特定日期的统计
                String dateCacheKey = "countByDate:" + dateStr;
                cacheService.evict(CommonCacheConfig.DIET_STATS_CACHE, dateCacheKey);

                // 清除特定日期的活跃用户列表
                String activeUsersCacheKey = "activeUsers:" + dateStr;
                cacheService.evict(CommonCacheConfig.DIET_STATS_CACHE, activeUsersCacheKey);

                // 清除包含该日期的日期范围缓存
                cacheService.evictByPattern(CommonCacheConfig.DIET_STATS_CACHE, "activeUsersRange:*" + dateStr + "*");

                // 清除热门食物缓存
                cacheService.evictByPattern(CommonCacheConfig.DIET_STATS_CACHE, "popular*");
            }

            // 3. 清除管理员查询的缓存
            cacheService.evictByPattern(CommonCacheConfig.DIET_RECORD_CACHE, "adminRecords:*");

            // 4. 发布特定用户和日期的缓存事件，通知其他服务
            if (userId != null) {
                String userCacheKey = "user:" + userId;
                cacheEventPublisher.publishEvictEvent(
                        CommonCacheConfig.DIET_RECORD_CACHE,
                        userCacheKey,
                        "diet-service");

                if (dateStr != null) {
                    String dateCacheKey = "date:" + dateStr;
                    cacheEventPublisher.publishEvictEvent(
                            CommonCacheConfig.DIET_RECORD_CACHE,
                            dateCacheKey,
                            "diet-service");
                }
            }
        } catch (Exception e) {
            log.error("清除饮食记录相关缓存失败", e);
        }
    }


    @Override
    public DietRecordResponseDTO getDietRecordDetail(Long userId, Long recordId) {
        // 构建缓存键
        String cacheKey = "detail:" + userId + ":" + recordId;

        // 尝试从缓存获取
        DietRecordResponseDTO cachedRecord = cacheService.get(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey);
        if (cachedRecord != null) {
            return cachedRecord;
        }

        log.debug("从数据库获取饮食记录详情: userId={}, recordId={}", userId, recordId);
        // 查询记录
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getId, recordId).eq(DietRecord::getUserId, userId);

        DietRecord dietRecord = dietRecordMapper.selectOne(wrapper);
        if (dietRecord == null) {
            return null;
        }

        DietRecordResponseDTO result = convertToResponseDTO(dietRecord);

        cacheService.putAsync(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey, result, CACHE_EXPIRATION_MINUTES);

        return result;
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
    public int countDietRecordsByDate(LocalDate date) {
        // 构建缓存键
        String cacheKey = "countByDate:" + date;

        // 尝试从缓存获取
        Integer cachedCount = cacheService.get(CommonCacheConfig.DIET_STATS_CACHE, cacheKey);
        if (cachedCount != null) {
            return cachedCount;
        }

        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getDate, date);

        // 统计记录数
        int count = Math.toIntExact(dietRecordMapper.selectCount(wrapper));

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_STATS_CACHE, cacheKey, count, CACHE_EXPIRATION_MINUTES);

        return count;
    }

    @Override
    public List<Long> findActiveUserIdsByDate(LocalDate date) {
        // 构建缓存键
        String cacheKey = "activeUsers:" + date;

        // 尝试从缓存获取
        List<Long> cachedUserIds = cacheService.get(CommonCacheConfig.DIET_STATS_CACHE, cacheKey);
        if (cachedUserIds != null) {
            return cachedUserIds;
        }

        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DietRecord::getDate, date)
                .select(DietRecord::getUserId)
                .groupBy(DietRecord::getUserId);

        // 查询并提取用户ID
        List<Long> userIds = dietRecordMapper.selectList(wrapper)
                .stream()
                .map(DietRecord::getUserId)
                .collect(Collectors.toList());

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_STATS_CACHE, cacheKey, userIds, CACHE_EXPIRATION_MINUTES);

        return userIds;
    }

    @Override
    public List<Long> findActiveUserIdsByDateRange(LocalDate startDate, LocalDate endDate) {
        // 构建缓存键
        String cacheKey = "activeUsersRange:" + startDate + ":" + endDate;

        // 尝试从缓存获取
        List<Long> cachedUserIds = cacheService.get(CommonCacheConfig.DIET_STATS_CACHE, cacheKey);
        if (cachedUserIds != null) {
            return cachedUserIds;
        }
        // 构建查询条件
        LambdaQueryWrapper<DietRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(DietRecord::getDate, startDate, endDate)
                .select(DietRecord::getUserId)
                .groupBy(DietRecord::getUserId);

        // 查询并提取用户ID
        List<Long> userIds = dietRecordMapper.selectList(wrapper)
                .stream()
                .map(DietRecord::getUserId)
                .collect(Collectors.toList());

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_STATS_CACHE, cacheKey, userIds, CACHE_EXPIRATION_MINUTES);

        return userIds;
    }


    @Override
    public DietRecordResponseDTO getAdminDietRecordDetail(Long recordId) {
        // 构建缓存键
        String cacheKey = "adminDetail:" + recordId;

        // 尝试从缓存获取
        DietRecordResponseDTO cachedRecord = cacheService.get(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey);
        if (cachedRecord != null) {
            return cachedRecord;
        }

        // 查询记录
        DietRecord dietRecord = dietRecordMapper.selectById(recordId);
        if (dietRecord == null) {
            return null;
        }

        DietRecordResponseDTO result = convertToResponseDTO(dietRecord);

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey, result, CACHE_EXPIRATION_MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getPopularFoods(LocalDate startDate, LocalDate endDate, int limit) {
        // 构建缓存键
        String cacheKey = "popularRange:" + startDate + ":" + endDate + ":" + limit;

        // 尝试从缓存获取
        List<Map<String, Object>> cachedData = cacheService.get(CommonCacheConfig.DIET_STATS_CACHE, cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

        log.debug("从数据库查询热门食物数据, 日期范围: {} 至 {}", startDate, endDate);
        // 从食物明细表查询该时间段内的热门食物
        List<Map<String, Object>> popularFoods = dietRecordMapper.findPopularFoods(startDate, endDate, limit);

        if (popularFoods == null) {
            return new ArrayList<>();
        }

        // 处理返回结果，确保符合前端期望的格式
        List<Map<String, Object>> result = popularFoods.stream().map(food -> {
            Map<String, Object> foodMap = new HashMap<>();
            foodMap.put("name", food.get("food_name"));
            foodMap.put("count", food.get("count"));
            return foodMap;
        }).collect(Collectors.toList());

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_STATS_CACHE, cacheKey, result, POPULAR_FOODS_CACHE_TTL);

        return result;
    }

    @Override
    public List<Map<String, Object>> getPopularFoodsByPeriod(String period, int limit) {
        // 构建缓存键
        String cacheKey = "popular:" + period + ":" + limit;

        // 尝试从缓存获取
        List<Map<String, Object>> cachedData = cacheService.get(CommonCacheConfig.DIET_STATS_CACHE, cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

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

        // 获取热门食物统计
        List<Map<String, Object>> popularFoods = getPopularFoods(startDate, endDate, limit);

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_STATS_CACHE, cacheKey, popularFoods, POPULAR_FOODS_CACHE_TTL);

        return popularFoods;
    }

    /**
     * 构建饮食记录缓存键 - 使用Command对象
     */
    private String buildRecordsCacheKey(DietRecordQueryCommand command) {
        StringBuilder keyBuilder = new StringBuilder("records:");
        keyBuilder.append(command.getUserId()).append(":");
        keyBuilder.append(command.getPage()).append(":");
        keyBuilder.append(command.getSize());

        if (StringUtils.isNotBlank(command.getStartDate())) {
            keyBuilder.append(":").append(command.getStartDate());
        } else {
            keyBuilder.append(":");
        }

        if (StringUtils.isNotBlank(command.getEndDate())) {
            keyBuilder.append(":").append(command.getEndDate());
        } else {
            keyBuilder.append(":");
        }

        if (StringUtils.isNotBlank(command.getMealType())) {
            keyBuilder.append(":").append(command.getMealType());
        } else {
            keyBuilder.append(":");
        }

        return keyBuilder.toString();
    }

    @Override
    @Transactional
    public Long addDietRecord(DietRecordAddCommand command) {

        //清除相关缓存
        clearRelatedCaches(command.getUserId(), command.getDate());
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

        return recordId;
    }

    @Override
    public PageResult<DietRecordResponseDTO> getDietRecords(DietRecordQueryCommand command) {
        // 生成缓存key
        String cacheKey = buildRecordsCacheKey(command);

        // 尝试从缓存获取
        PageResult<DietRecordResponseDTO> cachedResult = cacheService.get(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        log.debug("缓存未命中，从数据库查询");

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

        PageResult<DietRecordResponseDTO> result = PageResult.of(records, page.getTotal(), command.getPage(), command.getSize());

        cacheService.put(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey, result, CACHE_EXPIRATION_MINUTES);

        return result;
    }

    @Override
    public PageResult<DietRecordResponseDTO> getAllUsersDietRecords(DietRecordQueryCommand command) {
        // 构建缓存键
        String cacheKey = "adminRecords:" + (command.getUserId() == null ? "all" : command.getUserId()) + ":" +
                command.getPage() + ":" + command.getSize() + ":" +
                (command.getStartDate() == null ? "" : command.getStartDate()) + ":" +
                (command.getEndDate() == null ? "" : command.getEndDate()) + ":" +
                (command.getMealType() == null ? "" : command.getMealType());

        // 尝试从缓存获取
        PageResult<DietRecordResponseDTO> cachedResult = cacheService.get(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

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

        // 缓存结果
        cacheService.putAsync(CommonCacheConfig.DIET_RECORD_CACHE, cacheKey, result, CACHE_EXPIRATION_MINUTES);

        return result;
    }

    @Override
    @Transactional
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

        // 清除相关缓存
        clearRelatedCaches(dietRecord.getUserId(), dietRecord.getDate().format(DATE_FORMATTER));

        return true;
    }
}