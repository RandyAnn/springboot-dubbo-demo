package com.example.gateway.controller;

import com.example.common.command.food.FoodCategorySaveCommand;
import com.example.common.command.food.FoodCategoryUpdateCommand;
import com.example.common.command.food.FoodImageUpdateCommand;
import com.example.common.command.food.FoodQueryCommand;
import com.example.common.command.food.FoodSaveCommand;
import com.example.common.command.food.FoodUpdateCommand;
import com.example.common.dto.food.FoodCategoryDTO;
import com.example.common.dto.food.FoodImageUpdateRequestDTO;
import com.example.common.dto.food.FoodItemDTO;
import com.example.common.dto.food.FoodQueryRequestDTO;
import com.example.common.dto.food.FoodCreateRequestDTO;
import com.example.common.dto.food.FoodUpdateRequestDTO;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.FileService;
import com.example.common.service.FoodCategoryService;
import com.example.common.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员食物控制器
 * 提供管理员管理食物数据的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/food")
public class AdminFoodController {

    @DubboReference
    private FoodService foodService;

    @DubboReference
    private FileService fileService;

    @DubboReference
    private FoodCategoryService foodCategoryService;


    /**
     * 分页查询食物列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> getFoodById(@PathVariable("id") Integer id) {
        FoodItemDTO food = foodService.getFoodById(id);
        return ResponseEntity.ok(ApiResponse.success(food));
    }



    /**
     * 获取所有食物分类详情
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FoodCategoryDTO>>> getAllCategories() {
        List<FoodCategoryDTO> categories = foodCategoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 分页查询食物分类
     */
    @GetMapping("/category/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<FoodCategoryDTO>>> getCategoriesByPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "15") Integer size) {
        PageResult<FoodCategoryDTO> result = foodCategoryService.getCategoriesByPage(current, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取食物分类详情
     */
    @GetMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodCategoryDTO>> getCategoryById(@PathVariable("id") Integer id) {
        FoodCategoryDTO category = foodCategoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * 添加食物分类
     */
    @PostMapping("/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodCategoryDTO>> addCategory(@RequestBody FoodCategoryDTO categoryDTO) {
        // 创建命令对象
        FoodCategorySaveCommand command = new FoodCategorySaveCommand();
        BeanUtils.copyProperties(categoryDTO, command);

        FoodCategoryDTO savedCategory = foodCategoryService.saveCategory(command);
        return ResponseEntity.ok(ApiResponse.success(savedCategory));
    }

    /**
     * 更新食物分类
     */
    @PutMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateCategory(
            @PathVariable("id") Integer id,
            @RequestBody FoodCategoryDTO categoryDTO) {
        // 创建命令对象
        FoodCategoryUpdateCommand command = new FoodCategoryUpdateCommand();
        BeanUtils.copyProperties(categoryDTO, command);
        command.setId(id);

        boolean result = foodCategoryService.updateCategory(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 删除食物分类
     */
    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteCategory(@PathVariable("id") Integer id) {
        boolean result = foodCategoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 添加食物
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> addFood(@RequestBody FoodCreateRequestDTO requestDTO) {
        // 创建命令对象
        FoodSaveCommand command = new FoodSaveCommand();
        BeanUtils.copyProperties(requestDTO, command);

        // 调用服务方法
        FoodItemDTO savedFood = foodService.saveFood(command);
        return ResponseEntity.ok(ApiResponse.success(savedFood));
    }

    /**
     * 更新食物
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> updateFood(
            @PathVariable("id") Integer id,
            @RequestBody FoodUpdateRequestDTO requestDTO) {
        // 创建命令对象
        FoodUpdateCommand command = new FoodUpdateCommand();
        BeanUtils.copyProperties(requestDTO, command);
        command.setId(id);

        // 调用服务方法
        FoodItemDTO updatedFood = foodService.updateFood(command);
        return ResponseEntity.ok(ApiResponse.success(updatedFood));
    }

    /**
     * 删除食物
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteFood(@PathVariable("id") Integer id) {
        boolean result = foodService.deleteFood(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取食物图片上传URL
     */
    @GetMapping("/upload-image-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getUploadImageUrl(
            @RequestParam("foodId") Long foodId,
            @RequestParam("contentType") String contentType) {
        // 生成上传URL
        String uploadUrl = fileService.generateUploadPresignedUrl(
                foodId,
                "foodimage",
                contentType,
                30); // 30分钟有效期

        return ResponseEntity.ok(ApiResponse.success(uploadUrl));
    }

    /**
     * 更新食物图片URL
     */
    @PutMapping("/{id}/image-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateFoodImageUrl(
            @PathVariable("id") Integer id,
            @RequestBody FoodImageUpdateRequestDTO requestDTO) {
        // 创建命令对象
        FoodImageUpdateCommand command = FoodImageUpdateCommand.withFoodId(id);
        command.setImageUrl(requestDTO.getImageUrl());

        // 调用服务方法
        boolean result = foodService.updateFoodImageUrl(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 批量导入食物数据
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importFoods(@RequestBody Map<String, List<FoodItemDTO>> request) {
        List<FoodItemDTO> foods = request.get("foods");

        if (foods == null || foods.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "请提供有效的食物数据"));
        }

        Map<String, Object> result = foodService.batchImportFoods(foods);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
