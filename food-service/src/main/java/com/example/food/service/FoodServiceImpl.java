package com.example.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.command.FoodImageUpdateCommand;
import com.example.common.command.FoodQueryCommand;
import com.example.common.command.FoodSaveCommand;
import com.example.common.command.FoodUpdateCommand;
import com.example.common.cache.CacheService;
import com.example.common.config.cache.CommonCacheConfig;
import com.example.common.dto.FoodCategoryDTO;
import com.example.common.dto.FoodItemDTO;
import com.example.common.dto.FoodQueryDTO;
import com.example.common.entity.Food;
import com.example.common.response.PageResult;
import com.example.common.service.FileService;
import com.example.common.service.FoodCategoryService;
import com.example.common.service.FoodService;
import com.example.food.mapper.FoodMapper;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 食物服务实现类
 */
@Service
@DubboService
public class FoodServiceImpl extends ServiceImpl<FoodMapper, Food> implements FoodService {

    private static final Logger logger = LoggerFactory.getLogger(FoodServiceImpl.class);

    // 缓存过期时间（小时）
    private static final long CACHE_EXPIRATION_HOURS = 24; // 食物数据缓存24小时

    private final FoodMapper foodMapper;
    private final CacheService cacheService;

    @DubboReference
    private FileService fileService;

    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    public FoodServiceImpl(FoodMapper foodMapper, CacheService cacheService) {
        this.foodMapper = foodMapper;
        this.cacheService = cacheService;
    }

    @Override
    public PageResult<FoodItemDTO> queryFoodsByPage(FoodQueryCommand command) {
        // 构建缓存键
        String cacheKey = buildCacheKey(command);

        // 尝试从缓存获取
        PageResult<FoodItemDTO> cachedResult = cacheService.get(CommonCacheConfig.FOOD_INFO_CACHE, cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 构建查询条件
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.isNotBlank(command.getKeyword())) {
            wrapper.like(Food::getFoodName, command.getKeyword())
                   .or()
                   .like(Food::getMeasure, command.getKeyword());
        }

        // 分类筛选 - 使用categoryId
        if (command.getCategoryId() != null) {
            wrapper.eq(Food::getCategoryId, command.getCategoryId());
        }

        // 执行分页查询
        IPage<Food> page = new Page<>(command.getCurrent(), command.getSize());
        page = this.page(page, wrapper);

        // 转换结果
        List<FoodItemDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResult<FoodItemDTO> result = PageResult.of(records, page.getTotal(), command.getCurrent(), command.getSize());

        // 异步缓存结果
        cacheService.put(CommonCacheConfig.FOOD_INFO_CACHE, cacheKey, result, CACHE_EXPIRATION_HOURS * 60);

        return result;
    }



    /**
     * 构建缓存键
     */
    private String buildCacheKey(FoodQueryCommand command) {
        StringBuilder keyBuilder = new StringBuilder("list:");
        keyBuilder.append("page_").append(command.getCurrent());
        keyBuilder.append("_size_").append(command.getSize());

        if (command.getCategoryId() != null) {
            keyBuilder.append("_category_").append(command.getCategoryId());
        }

        if (StringUtils.isNotBlank(command.getKeyword())) {
            keyBuilder.append("_keyword_").append(command.getKeyword());
        }

        return keyBuilder.toString();
    }



    @Override
    public FoodItemDTO getFoodById(Integer id) {
        if (id == null) {
            return null;
        }

        // 尝试从缓存获取
        FoodItemDTO cachedFood = cacheService.get(CommonCacheConfig.FOOD_INFO_CACHE, id);
        if (cachedFood != null) {
            return cachedFood;
        }

        logger.debug("从数据库获取食物信息: id={}", id);
        Food food = this.getById(id);
        if (food == null) {
            return null;
        }

        // 转换为DTO
        FoodItemDTO foodDTO = convertToDTO(food);

        // 异步缓存结果
        cacheService.put(CommonCacheConfig.FOOD_INFO_CACHE, id, foodDTO, CACHE_EXPIRATION_HOURS * 60);

        return foodDTO;
    }



    @Override
    public List<String> getAllCategories() {
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<String> cachedCategories = cacheService.get(CommonCacheConfig.FOOD_INFO_CACHE, "categories");
        if (cachedCategories != null) {
            return cachedCategories;
        }

        logger.debug("从数据库获取食物分类列表");

        // 通过FoodCategoryService获取所有分类
        List<FoodCategoryDTO> categoryDTOs = foodCategoryService.getAllCategories();
        List<String> categories = categoryDTOs.stream()
                .map(FoodCategoryDTO::getName)
                .collect(Collectors.toList());

        // 异步缓存结果
        cacheService.putAsync(CommonCacheConfig.FOOD_INFO_CACHE, "categories", categories, CACHE_EXPIRATION_HOURS * 60);

        return categories;
    }



    /**
     * 将Food实体转换为DTO
     */
    private FoodItemDTO convertToDTO(Food food) {
        FoodItemDTO dto = new FoodItemDTO();

        dto.setId(food.getId());
        dto.setName(food.getFoodName());
        dto.setMeasure(food.getMeasure());
        dto.setCategoryId(food.getCategoryId());

        // 设置分类名称 - 通过categoryId查询
        if (food.getCategoryId() != null) {
            FoodCategoryDTO category = foodCategoryService.getCategoryById(food.getCategoryId());
            if (category != null) {
                dto.setCategory(category.getName());
            } else {
                dto.setCategory(""); // 如果找不到分类，设置为空字符串
            }
        } else {
            dto.setCategory(""); // 如果没有分类ID，设置为空字符串
        }

        // 设置图片URL，如果有原始URL，则生成预签名下载URL（有效期60分钟）
        if (StringUtils.isNotBlank(food.getImageUrl())) {
            try {
                String downloadUrl = fileService.generateDownloadPresignedUrl(food.getImageUrl(), 60);
                dto.setImageUrl(downloadUrl);
            } catch (Exception e) {
                logger.error("生成食物图片下载URL失败: id={}, error={}", food.getId(), e.getMessage());
                // 如果生成失败，仍然设置原始URL
                dto.setImageUrl(food.getImageUrl());
            }
        } else {
            dto.setImageUrl(food.getImageUrl()); // 可能为null或空字符串
        }

        // 转换数值类型
        dto.setGrams(parseDoubleOrDefault(food.getGrams(), 0.0));
        dto.setCalories(parseDoubleOrDefault(food.getCalories(), 0.0));
        dto.setProtein(parseDoubleOrDefault(food.getProtein(), 0.0));
        dto.setFat(parseDoubleOrDefault(food.getFat(), 0.0));
        dto.setCarbs(parseDoubleOrDefault(food.getCarbs(), 0.0));

        // 处理饱和脂肪
        dto.setSatFat(parseDoubleOrDefault(food.getSatFat(), 0.0));

        // 设置描述和单位
        dto.setDesc(food.getMeasure());

        // 直接使用measure作为单位，不提取
        dto.setUnit(StringUtils.isNotBlank(food.getMeasure()) ?
                     food.getMeasure() : "100g");

        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private Food convertToEntity(FoodItemDTO dto) {
        Food food = new Food();

        if (dto.getId() != null) {
            food.setId(dto.getId());
        }

        food.setFoodName(dto.getName());
        food.setMeasure(dto.getMeasure());

        // 设置分类ID
        food.setCategoryId(dto.getCategoryId());

        food.setImageUrl(dto.getImageUrl());

        // 转换数值类型
        food.setGrams(dto.getGrams() != null ? String.valueOf(dto.getGrams()) : null);
        food.setCalories(dto.getCalories() != null ? String.valueOf(dto.getCalories()) : null);
        food.setProtein(dto.getProtein() != null ? String.valueOf(dto.getProtein()) : null);
        food.setFat(dto.getFat() != null ? String.valueOf(dto.getFat()) : null);
        food.setCarbs(dto.getCarbs() != null ? String.valueOf(dto.getCarbs()) : null);
        food.setSatFat(dto.getSatFat() != null ? String.valueOf(dto.getSatFat()) : null);

        return food;
    }

    /**
     * 保存食物信息 - 使用Command对象
     */
    @Override
    public FoodItemDTO saveFood(FoodSaveCommand command) {
        // 将Command对象转换为实体
        Food food = new Food();
        food.setFoodName(command.getName());
        food.setMeasure(command.getMeasure());
        food.setCategoryId(command.getCategoryId());
        food.setImageUrl(command.getImageUrl());

        // 转换数值类型
        food.setGrams(command.getGrams() != null ? String.valueOf(command.getGrams()) : null);
        food.setCalories(command.getCalories() != null ? String.valueOf(command.getCalories()) : null);
        food.setProtein(command.getProtein() != null ? String.valueOf(command.getProtein()) : null);
        food.setFat(command.getFat() != null ? String.valueOf(command.getFat()) : null);
        food.setCarbs(command.getCarbs() != null ? String.valueOf(command.getCarbs()) : null);
        food.setSatFat(command.getSatFat() != null ? String.valueOf(command.getSatFat()) : null);

        this.save(food);

        // 清除缓存
        cacheService.evictAsync(CommonCacheConfig.FOOD_INFO_CACHE, "categories");
        cacheService.evictAsync(CommonCacheConfig.FOOD_INFO_CACHE, "list:*");

        return convertToDTO(food);
    }



    /**
     * 更新食物信息 - 使用Command对象
     */
    @Override
    public FoodItemDTO updateFood(FoodUpdateCommand command) {
        // 获取旧的食物信息，以便检查图片是否变化
        Food oldFood = this.getById(command.getId());
        if (oldFood == null) {
            // 如果食物不存在，直接返回null
            return null;
        }

        // 获取旧的图片路径
        String oldImagePath = oldFood.getImageUrl();

        // 将Command对象转换为实体
        Food food = new Food();
        food.setId(command.getId());
        food.setFoodName(command.getName());
        food.setMeasure(command.getMeasure());
        food.setCategoryId(command.getCategoryId());
//        food.setImageUrl(command.getImageUrl());

        // 转换数值类型
        food.setGrams(command.getGrams() != null ? String.valueOf(command.getGrams()) : null);
        food.setCalories(command.getCalories() != null ? String.valueOf(command.getCalories()) : null);
        food.setProtein(command.getProtein() != null ? String.valueOf(command.getProtein()) : null);
        food.setFat(command.getFat() != null ? String.valueOf(command.getFat()) : null);
        food.setCarbs(command.getCarbs() != null ? String.valueOf(command.getCarbs()) : null);
        food.setSatFat(command.getSatFat() != null ? String.valueOf(command.getSatFat()) : null);

        boolean result = this.updateById(food);

        if (result) {
            // 清除缓存
            cacheService.evict(CommonCacheConfig.FOOD_INFO_CACHE, food.getId());
            cacheService.clear(CommonCacheConfig.FOOD_INFO_CACHE); // 清除所有食物相关缓存

            // 检查图片是否变化
            String newImagePath = food.getImageUrl();
            if (oldImagePath != null && !oldImagePath.isEmpty() &&
                !oldImagePath.equals(newImagePath)) {
                try {
                    // 异步删除旧图片
                    CompletableFuture.runAsync(() -> {
                        try {
                            fileService.deleteFile(oldImagePath);
                            logger.debug("成功删除旧食物图片: {}", oldImagePath);
                        } catch (Exception e) {
                            logger.error("删除旧食物图片文件失败：" + e.getMessage(), e);
                        }
                    });
                } catch (Exception e) {
                    // 删除旧图片失败不影响更新操作，只记录错误
                    logger.error("删除旧食物图片文件失败：" + e.getMessage(), e);
                }
            }
        }

        return convertToDTO(food);
    }



    /**
     * 删除食物
     */
    @Override
    public boolean deleteFood(Integer id) {
        // 先获取食物信息，以便删除图片
        Food food = this.getById(id);
        if (food == null) {
            return false;
        }

        // 获取图片路径
        String imagePath = food.getImageUrl();

        // 删除食物记录
        boolean result = this.removeById(id);

        if (result) {
            // 清除缓存
            cacheService.evictAsync(CommonCacheConfig.FOOD_INFO_CACHE, id);
            cacheService.clearAsync(CommonCacheConfig.FOOD_INFO_CACHE); // 清除所有食物相关缓存

            // 如果存在图片，则异步删除
            if (imagePath != null && !imagePath.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        fileService.deleteFile(imagePath);
                        logger.debug("成功删除食物图片: {}", imagePath);
                    } catch (Exception e) {
                        logger.error("删除食物图片文件失败：" + e.getMessage(), e);
                    }
                });
            }
        }

        return result;
    }

    /**
     * 更新食物图片URL - 使用Command对象
     */
    @Override
    public boolean updateFoodImageUrl(FoodImageUpdateCommand command) {
        Food food = this.getById(command.getFoodId());
        if (food == null) {
            return false;
        }

        // 获取旧的图片路径
        String oldImagePath = food.getImageUrl();

        // 设置新的图片路径
        food.setImageUrl(command.getImageUrl());
        boolean result = this.updateById(food);

        if (result) {
            // 清除缓存
            cacheService.evictAsync(CommonCacheConfig.FOOD_INFO_CACHE, command.getFoodId());
            cacheService.evictAsync(CommonCacheConfig.FOOD_INFO_CACHE, "list:*");

            // 如果存在旧图片，则异步删除
            if (oldImagePath != null && !oldImagePath.isEmpty() && !oldImagePath.equals(command.getImageUrl())) {
                CompletableFuture.runAsync(() -> {
                    try {
                        fileService.deleteFile(oldImagePath);
                        logger.debug("成功删除旧食物图片: {}", oldImagePath);
                    } catch (Exception e) {
                        logger.error("删除旧食物图片文件失败：" + e.getMessage(), e);
                    }
                });
            }
        }

        return result;
    }



    /**
     * 安全地将String转为Double，处理null值和格式异常
     * @param value 要转换的字符串值
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的Double值或默认值
     */
    private Double parseDoubleOrDefault(String value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Map<String, Object> batchImportFoods(List<FoodItemDTO> foods) {
        if (foods == null || foods.isEmpty()) {
            throw new IllegalArgumentException("导入的食物数据不能为空");
        }

        logger.info("开始批量导入食物数据，总数量: {}", foods.size());

        Map<String, Object> result = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        // 批量处理食物数据
        for (FoodItemDTO foodDTO : foods) {
            try {
                // 验证必填字段
                if (isInvalidFoodData(foodDTO)) {
                    String errorMsg = "食物数据不完整: " + (foodDTO.getName() != null ? foodDTO.getName() : "未命名食物");
                    errorMessages.add(errorMsg);
                    failCount++;
                    continue;
                }

                // 转换为实体对象
                Food food = convertToEntity(foodDTO);

                // 保存到数据库
                this.save(food);

                successCount++;
                logger.debug("成功导入食物: {}", food.getFoodName());
            } catch (Exception e) {
                String errorMsg = "导入食物失败: " + (foodDTO.getName() != null ? foodDTO.getName() : "未命名食物") + ", 原因: " + e.getMessage();
                errorMessages.add(errorMsg);
                failCount++;
                logger.error("导入食物数据异常", e);
            }
        }

        // 清除所有食物相关缓存
        cacheService.clearAsync(CommonCacheConfig.FOOD_INFO_CACHE);
        // 注意：不再清除FOOD_CATEGORY_CACHE，这应该由FoodCategoryService负责

        // 返回结果
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errorMessages", errorMessages);

        logger.info("批量导入食物数据完成，成功: {}, 失败: {}", successCount, failCount);

        return result;
    }

    /**
     * 验证食物数据是否有效
     */
    private boolean isInvalidFoodData(FoodItemDTO foodDTO) {
        return foodDTO == null
                || StringUtils.isBlank(foodDTO.getName())
                || foodDTO.getCategoryId() == null
                || foodDTO.getCalories() == null
                || foodDTO.getProtein() == null
                || foodDTO.getFat() == null
                || foodDTO.getCarbs() == null
                || StringUtils.isBlank(foodDTO.getMeasure())
                || foodDTO.getGrams() == null;
    }


}