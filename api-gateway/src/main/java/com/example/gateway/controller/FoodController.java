package com.example.gateway.controller;

import com.example.food.command.FoodQueryCommand;
import com.example.food.dto.FoodCategoryDTO;
import com.example.food.dto.FoodItemDTO;
import com.example.food.dto.FoodQueryRequestDTO;
import org.springframework.beans.BeanUtils;
import com.example.shared.response.ApiResponse;
import com.example.shared.response.PageResult;
import com.example.food.service.FoodCategoryService;
import com.example.food.service.FoodService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食物控制器，提供食物相关的接口
 */
@RestController
@RequestMapping("/api/food")
public class FoodController {

    @DubboReference
    private FoodService foodService;

    @DubboReference
    private FoodCategoryService foodCategoryService;

    /**
     * 分页查询食物列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResult<FoodItemDTO>>> getFoodsByPage(FoodQueryRequestDTO requestDTO) {
        // 转换为Command对象
        FoodQueryCommand command = new FoodQueryCommand();
        BeanUtils.copyProperties(requestDTO, command);

        // 调用服务方法
        PageResult<FoodItemDTO> result = foodService.queryFoodsByPage(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取食物详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemDTO>> getFoodById(@PathVariable("id") Integer id) {
        FoodItemDTO food = foodService.getFoodById(id);
        return ResponseEntity.ok(ApiResponse.success(food));
    }



    /**
     * 获取所有食物分类详情
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<FoodCategoryDTO>>> getAllCategories() {
        List<FoodCategoryDTO> categories = foodCategoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}