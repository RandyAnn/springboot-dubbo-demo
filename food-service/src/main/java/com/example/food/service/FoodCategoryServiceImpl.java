package com.example.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.food.command.FoodCategorySaveCommand;
import com.example.food.command.FoodCategoryUpdateCommand;
import com.example.food.dto.FoodCategoryDTO;
import com.example.food.entity.FoodCategory;
import com.example.shared.response.PageResult;
import com.example.food.service.FoodCategoryService;
import com.example.food.mapper.FoodCategoryMapper;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 食物分类服务实现类
 */
@Service
@DubboService
public class FoodCategoryServiceImpl extends ServiceImpl<FoodCategoryMapper, FoodCategory> implements FoodCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(FoodCategoryServiceImpl.class);

    private final FoodCategoryMapper foodCategoryMapper;

    @Autowired
    public FoodCategoryServiceImpl(FoodCategoryMapper foodCategoryMapper) {
        this.foodCategoryMapper = foodCategoryMapper;
    }

    @Override
    @Cacheable(value = "foodCategory", key = "'page_' + #current + '_size_' + #size")
    public PageResult<FoodCategoryDTO> getCategories(Integer current, Integer size) {
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

        return PageResult.of(records, page.getTotal(), current, size);
    }



    @Override
    @Cacheable(value = "foodCategory", key = "#id", unless = "#result == null")
    public FoodCategoryDTO getCategoryById(Integer id) {
        if (id == null) {
            return null;
        }

        logger.debug("从数据库获取食物分类信息: id={}", id);
        FoodCategory category = this.getById(id);
        if (category == null) {
            return null;
        }

        // 转换为DTO
        return convertToDTO(category);
    }

    @Override
    @CacheEvict(value = "foodCategory", allEntries = true)
    public FoodCategoryDTO saveCategory(FoodCategorySaveCommand command) {
        // 转换为实体
        FoodCategory category = new FoodCategory();
        category.setName(command.getName());
        category.setDescription(command.getDescription());
        category.setColor(command.getColor());
        category.setSortOrder(command.getSortOrder());

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        // 保存到数据库
        this.save(category);

        // 转换为DTO并返回
        return convertToDTO(category);
    }

    @Override
    @CacheEvict(value = "foodCategory", allEntries = true)
    public boolean updateCategory(FoodCategoryUpdateCommand command) {
        // 构建更新实体
        FoodCategory category = new FoodCategory();
        category.setId(command.getId());
        category.setName(command.getName());
        category.setDescription(command.getDescription());
        category.setColor(command.getColor());
        category.setSortOrder(command.getSortOrder());

        // 设置更新时间
        category.setUpdatedAt(LocalDateTime.now());

        // 直接更新到数据库
        return this.updateById(category);
    }

    @Override
    @CacheEvict(value = "foodCategory", allEntries = true)
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
        return this.removeById(id);
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


}
