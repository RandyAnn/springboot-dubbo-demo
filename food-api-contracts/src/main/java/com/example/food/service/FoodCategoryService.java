package com.example.food.service;

import com.example.food.command.FoodCategorySaveCommand;
import com.example.food.command.FoodCategoryUpdateCommand;
import com.example.food.dto.FoodCategoryDTO;
import com.example.shared.response.PageResult;

import java.util.List;

/**
 * 食物分类服务接口
 */
public interface FoodCategoryService {

    /**
     * 分页查询食物分类
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<FoodCategoryDTO> getCategories(Integer current, Integer size);

    /**
     * 根据ID获取食物分类
     * @param id 分类ID
     * @return 分类详情
     */
    FoodCategoryDTO getCategoryById(Integer id);

    /**
     * 保存食物分类
     * @param command 分类保存命令对象
     * @return 保存后的分类信息
     */
    FoodCategoryDTO saveCategory(FoodCategorySaveCommand command);

    /**
     * 更新食物分类
     * @param command 分类更新命令对象
     * @return 是否更新成功
     */
    boolean updateCategory(FoodCategoryUpdateCommand command);

    /**
     * 删除食物分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Integer id);
}
