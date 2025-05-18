package com.example.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.FoodCategoryDTO;
import com.example.common.entity.FoodCategory;
import com.example.common.response.PageResult;
import com.example.common.service.FoodCategoryService;
import com.example.food.mapper.FoodCategoryMapper;
import org.apache.commons.lang3.StringUtils;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 食物分类服务实现类
 */
@Service
@DubboService
public class FoodCategoryServiceImpl extends ServiceImpl<FoodCategoryMapper, FoodCategory> implements FoodCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(FoodCategoryServiceImpl.class);

    // Redis缓存相关常量
    private static final String REDIS_CATEGORY_KEY_PREFIX = "food:category:detail:";
    private static final String REDIS_CATEGORIES_LIST_KEY = "food:category:list";
    private static final String REDIS_CATEGORIES_PAGE_KEY_PREFIX = "food:category:page:";
    private static final int REDIS_CACHE_EXPIRATION_HOURS = 24; // 分类数据缓存24小时

    private final FoodCategoryMapper foodCategoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public FoodCategoryServiceImpl(FoodCategoryMapper foodCategoryMapper,
                                  RedisTemplate<String, Object> redisTemplate) {
        this.foodCategoryMapper = foodCategoryMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Cacheable(value = CommonCacheConfig.FOOD_CATEGORY_CACHE, key = "'all'")
    public List<FoodCategoryDTO> getAllCategories() {
        // 尝试从Redis缓存获取
        @SuppressWarnings("unchecked")
        List<FoodCategoryDTO> cachedCategories = (List<FoodCategoryDTO>) redisTemplate.opsForValue().get(REDIS_CATEGORIES_LIST_KEY);
        if (cachedCategories != null) {
            return cachedCategories;
        }

        logger.debug("从数据库获取食物分类列表");
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FoodCategory::getSortOrder);

        List<FoodCategory> categories = this.list(wrapper);
        List<FoodCategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 异步缓存到Redis
        CompletableFuture.runAsync(() -> cacheCategoryListToRedis(categoryDTOs));

        return categoryDTOs;
    }

    /**
     * 异步将分类列表缓存到Redis
     */
    @Async
    public void cacheCategoryListToRedis(List<FoodCategoryDTO> categories) {
        try {
            if (!categories.isEmpty()) {
                redisTemplate.opsForValue().set(
                        REDIS_CATEGORIES_LIST_KEY,
                        categories,
                        REDIS_CACHE_EXPIRATION_HOURS,
                        TimeUnit.HOURS
                );
            }
        } catch (Exception e) {
            logger.error("缓存食物分类列表到Redis失败", e);
        }
    }

    @Override
    public PageResult<FoodCategoryDTO> getCategoriesByPage(Integer current, Integer size) {
        // 构建Redis缓存键
        String redisCacheKey = REDIS_CATEGORIES_PAGE_KEY_PREFIX + "page_" + current + "_size_" + size;

        // 尝试从Redis缓存获取
        PageResult<FoodCategoryDTO> cachedResult = getCategoryPageFromRedis(redisCacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 构建分页查询
        IPage<FoodCategory> page = new Page<>(current, size);
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FoodCategory::getSortOrder);

        // 执行分页查询
        page = this.page(page, wrapper);

        // 转换结果
        List<FoodCategoryDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResult<FoodCategoryDTO> result = PageResult.of(records, page.getTotal(), current, size);

        // 异步缓存到Redis
        CompletableFuture.runAsync(() -> cacheCategoryPageToRedis(redisCacheKey, result));

        return result;
    }

    /**
     * 从Redis获取分类分页数据
     */
    private PageResult<FoodCategoryDTO> getCategoryPageFromRedis(String cacheKey) {
        try {
            Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult instanceof PageResult) {
                return (PageResult<FoodCategoryDTO>) cachedResult;
            }
        } catch (Exception e) {
            logger.error("从Redis获取食物分类分页数据失败", e);
        }
        return null;
    }

    /**
     * 异步将分类分页数据缓存到Redis
     */
    @Async
    public void cacheCategoryPageToRedis(String cacheKey, PageResult<FoodCategoryDTO> result) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    result,
                    REDIS_CACHE_EXPIRATION_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            logger.error("缓存食物分类分页数据到Redis失败", e);
        }
    }

    @Override
    @Cacheable(value = CommonCacheConfig.FOOD_CATEGORY_CACHE, key = "#id", unless = "#result == null")
    public FoodCategoryDTO getCategoryById(Integer id) {
        if (id == null) {
            return null;
        }

        // 构建缓存键
        String redisCacheKey = REDIS_CATEGORY_KEY_PREFIX + id;

        // 尝试从Redis缓存获取
        FoodCategoryDTO cachedCategory = getCategoryFromRedis(redisCacheKey);
        if (cachedCategory != null) {
            return cachedCategory;
        }

        logger.debug("从数据库获取食物分类信息: id={}", id);
        FoodCategory category = this.getById(id);
        if (category == null) {
            return null;
        }

        // 转换为DTO
        FoodCategoryDTO categoryDTO = convertToDTO(category);

        // 异步缓存到Redis
        CompletableFuture.runAsync(() -> cacheCategoryToRedis(redisCacheKey, categoryDTO));

        return categoryDTO;
    }

    /**
     * 从Redis获取分类数据
     */
    private FoodCategoryDTO getCategoryFromRedis(String cacheKey) {
        try {
            Object cachedCategory = redisTemplate.opsForValue().get(cacheKey);
            if (cachedCategory instanceof FoodCategoryDTO) {
                return (FoodCategoryDTO) cachedCategory;
            }
        } catch (Exception e) {
            logger.error("从Redis获取食物分类数据失败", e);
        }
        return null;
    }

    /**
     * 异步将分类数据缓存到Redis
     */
    @Async
    public void cacheCategoryToRedis(String cacheKey, FoodCategoryDTO categoryDTO) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    categoryDTO,
                    REDIS_CACHE_EXPIRATION_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            logger.error("缓存食物分类数据到Redis失败", e);
        }
    }

    @Override
    public FoodCategoryDTO getCategoryByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        // 构建查询条件
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodCategory::getName, name);

        // 执行查询
        FoodCategory category = this.getOne(wrapper);
        if (category == null) {
            return null;
        }

        // 转换为DTO
        return convertToDTO(category);
    }

    @Override
    public Integer getCategoryIdByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        return foodCategoryMapper.selectIdByName(name);
    }

    @Override
    @CacheEvict(value = CommonCacheConfig.FOOD_CATEGORY_CACHE, allEntries = true)
    public FoodCategoryDTO saveCategory(FoodCategoryDTO categoryDTO) {
        // 转换为实体
        FoodCategory category = convertToEntity(categoryDTO);

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        // 保存到数据库
        this.save(category);

        // 异步清除Redis缓存
        CompletableFuture.runAsync(() -> {
            clearRedisCategoryCache(null);
        });

        // 转换为DTO并返回
        return convertToDTO(category);
    }

    @Override
    @CacheEvict(value = CommonCacheConfig.FOOD_CATEGORY_CACHE, allEntries = true)
    public FoodCategoryDTO updateCategory(FoodCategoryDTO categoryDTO) {
        // 获取旧的分类信息
        FoodCategory oldCategory = this.getById(categoryDTO.getId());
        if (oldCategory == null) {
            // 如果分类不存在，直接返回null
            return null;
        }

        // 转换并更新分类信息
        FoodCategory category = convertToEntity(categoryDTO);

        // 设置更新时间
        category.setUpdatedAt(LocalDateTime.now());

        // 保留创建时间
        category.setCreatedAt(oldCategory.getCreatedAt());

        // 更新到数据库
        boolean result = this.updateById(category);

        // 清除缓存
        if (result) {
            // 异步清除Redis缓存
            CompletableFuture.runAsync(() -> {
                clearRedisCategoryCache(category.getId());
                clearRedisCategoryCache(null);
            });
        }

        // 转换为DTO并返回
        return convertToDTO(category);
    }

    @Override
    @CacheEvict(value = CommonCacheConfig.FOOD_CATEGORY_CACHE, allEntries = true)
    public boolean deleteCategory(Integer id) {
        // 检查分类是否存在
        FoodCategory category = this.getById(id);
        if (category == null) {
            return false;
        }

        // 检查分类下是否有食物
        Integer foodCount = foodCategoryMapper.countFoodByCategoryId(id);
        if (foodCount > 0) {
            // 如果分类下有食物，不允许删除
            logger.warn("分类下有{}个食物，不允许删除", foodCount);
            return false;
        }

        // 删除分类
        boolean result = this.removeById(id);

        // 清除缓存
        if (result) {
            // 异步清除Redis缓存
            CompletableFuture.runAsync(() -> {
                clearRedisCategoryCache(id);
                clearRedisCategoryCache(null);
            });
        }

        return result;
    }

    /**
     * 清除Redis分类缓存
     * @param id 分类ID，如果为null则清除所有分类缓存
     */
    private void clearRedisCategoryCache(Integer id) {
        try {
            if (id != null) {
                // 清除特定分类缓存
                String cacheKey = REDIS_CATEGORY_KEY_PREFIX + id;
                redisTemplate.delete(cacheKey);
            } else {
                // 清除所有分类相关缓存
                redisTemplate.keys(REDIS_CATEGORY_KEY_PREFIX + "*").forEach(key -> {
                    redisTemplate.delete(key);
                });

                redisTemplate.keys(REDIS_CATEGORIES_PAGE_KEY_PREFIX + "*").forEach(key -> {
                    redisTemplate.delete(key);
                });

                redisTemplate.delete(REDIS_CATEGORIES_LIST_KEY);
            }
        } catch (Exception e) {
            logger.error("清除Redis分类缓存失败", e);
        }
    }

    /**
     * 将FoodCategory实体转换为DTO
     */
    private FoodCategoryDTO convertToDTO(FoodCategory category) {
        FoodCategoryDTO dto = new FoodCategoryDTO();

        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setColor(category.getColor());
        dto.setSortOrder(category.getSortOrder());

        // 获取分类下的食物数量
        Integer foodCount = foodCategoryMapper.countFoodByCategoryId(category.getId());
        dto.setFoodCount(foodCount);

        return dto;
    }

    /**
     * 将DTO转换为FoodCategory实体
     */
    private FoodCategory convertToEntity(FoodCategoryDTO dto) {
        FoodCategory category = new FoodCategory();

        if (dto.getId() != null) {
            category.setId(dto.getId());
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setColor(dto.getColor());
        category.setSortOrder(dto.getSortOrder());

        return category;
    }
}
