package com.example.common.service;

import com.example.common.command.FoodImageUpdateCommand;
import com.example.common.command.FoodQueryCommand;
import com.example.common.command.FoodSaveCommand;
import com.example.common.command.FoodUpdateCommand;
import com.example.common.dto.FoodItemDTO;
import com.example.common.dto.FoodQueryDTO;
import com.example.common.response.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 食物服务接口
 */
public interface FoodService {
    /**
     * 分页查询食物列表
     * @param command 查询命令对象
     * @return 分页结果
     */
    PageResult<FoodItemDTO> queryFoodsByPage(FoodQueryCommand command);



    /**
     * 根据ID获取食物详情
     * @param id 食物ID
     * @return 食物详情
     */
    FoodItemDTO getFoodById(Integer id);

    /**
     * 获取所有食物分类
     * @return 分类列表
     */
    List<String> getAllCategories();

    /**
     * 保存食物信息
     * @param command 食物保存命令对象
     * @return 保存后的食物信息
     */
    FoodItemDTO saveFood(FoodSaveCommand command);



    /**
     * 更新食物信息
     * @param command 食物更新命令对象
     * @return 更新后的食物信息
     */
    FoodItemDTO updateFood(FoodUpdateCommand command);



    /**
     * 删除食物
     * @param id 食物ID
     * @return 是否删除成功
     */
    boolean deleteFood(Integer id);

    /**
     * 更新食物图片URL
     * @param command 食物图片更新命令对象
     * @return 是否更新成功
     */
    boolean updateFoodImageUrl(FoodImageUpdateCommand command);



    /**
     * 批量导入食物数据
     * @param foods 食物数据列表
     * @return 导入结果，包含成功数量和失败数量
     */
    Map<String, Object> batchImportFoods(List<FoodItemDTO> foods);
}