package com.example.common.service;

import com.example.common.dto.FoodCategoryDTO;
import com.example.common.response.PageResult;

import java.util.List;

/**
 * 食物分类服务接口
 */
public interface FoodCategoryService {
    /**
     * 获取所有食物分类
     * @return 分类列表
     */
    List<FoodCategoryDTO> getAllCategories();
    
    /**
     * 分页查询食物分类
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<FoodCategoryDTO> getCategoriesByPage(Integer current, Integer size);
    
    /**
     * 根据ID获取食物分类
     * @param id 分类ID
     * @return 分类详情
     */
    FoodCategoryDTO getCategoryById(Integer id);
    
    /**
     * 根据名称获取食物分类
     * @param name 分类名称
     * @return 分类详情
     */
    FoodCategoryDTO getCategoryByName(String name);
    
    /**
     * 根据名称获取分类ID
     * @param name 分类名称
     * @return 分类ID
     */
    Integer getCategoryIdByName(String name);
    
    /**
     * 保存食物分类
     * @param categoryDTO 分类信息
     * @return 保存后的分类信息
     */
    FoodCategoryDTO saveCategory(FoodCategoryDTO categoryDTO);
    
    /**
     * 更新食物分类
     * @param categoryDTO 分类信息
     * @return 更新后的分类信息
     */
    FoodCategoryDTO updateCategory(FoodCategoryDTO categoryDTO);
    
    /**
     * 删除食物分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Integer id);
}
