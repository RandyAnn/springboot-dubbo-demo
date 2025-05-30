package com.example.gateway.controller;

import com.example.common.command.FoodCategorySaveCommand;
import com.example.common.command.FoodCategoryUpdateCommand;
import com.example.common.command.FoodImageUpdateCommand;
import com.example.common.command.FoodQueryCommand;
import com.example.common.command.FoodSaveCommand;
import com.example.common.command.FoodUpdateCommand;
import com.example.common.dto.FoodCategoryDTO;
import com.example.common.dto.FoodImageUpdateRequestDTO;
import com.example.common.dto.FoodItemDTO;
import com.example.common.dto.FoodPageRequestDTO;
import com.example.common.dto.FoodQueryDTO;
import com.example.common.dto.FoodSaveRequestDTO;
import com.example.common.dto.FoodUpdateRequestDTO;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResult;
import com.example.common.service.FileService;
import com.example.common.service.FoodCategoryService;
import com.example.common.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ApiResponse<PageResult<FoodItemDTO>>> getFoodsByPage(FoodPageRequestDTO requestDTO) {
        try {
            // 转换为Command对象
            FoodQueryCommand command = new FoodQueryCommand();
            BeanUtils.copyProperties(requestDTO, command);

            // 调用服务方法
            PageResult<FoodItemDTO> result = foodService.queryFoodsByPage(command);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分页查询食物列表失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "分页查询食物列表失败"));
        }
    }

    /**
     * 获取食物详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> getFoodById(@PathVariable("id") Integer id) {
        try {
            FoodItemDTO food = foodService.getFoodById(id);
            return ResponseEntity.ok(ApiResponse.success(food));
        } catch (Exception e) {
            log.error("获取食物详情失败，食物ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取食物详情失败"));
        }
    }



    /**
     * 获取所有食物分类详情
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FoodCategoryDTO>>> getAllCategories() {
        try {
            List<FoodCategoryDTO> categories = foodCategoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("获取所有食物分类失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取所有食物分类失败"));
        }
    }

    /**
     * 分页查询食物分类
     */
    @GetMapping("/category/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<FoodCategoryDTO>>> getCategoriesByPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "15") Integer size) {
        try {
            PageResult<FoodCategoryDTO> result = foodCategoryService.getCategoriesByPage(current, size);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分页查询食物分类失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "分页查询食物分类失败"));
        }
    }

    /**
     * 获取食物分类详情
     */
    @GetMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodCategoryDTO>> getCategoryById(@PathVariable("id") Integer id) {
        try {
            FoodCategoryDTO category = foodCategoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            log.error("获取食物分类详情失败，分类ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取食物分类详情失败"));
        }
    }

    /**
     * 添加食物分类
     */
    @PostMapping("/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodCategoryDTO>> addCategory(@RequestBody FoodCategoryDTO categoryDTO) {
        try {
            // 创建命令对象
            FoodCategorySaveCommand command = new FoodCategorySaveCommand();
            BeanUtils.copyProperties(categoryDTO, command);

            FoodCategoryDTO savedCategory = foodCategoryService.saveCategory(command);
            return ResponseEntity.ok(ApiResponse.success(savedCategory));
        } catch (Exception e) {
            log.error("添加食物分类失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "添加食物分类失败"));
        }
    }

    /**
     * 更新食物分类
     */
    @PutMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateCategory(
            @PathVariable("id") Integer id,
            @RequestBody FoodCategoryDTO categoryDTO) {
        try {
            // 创建命令对象
            FoodCategoryUpdateCommand command = new FoodCategoryUpdateCommand();
            BeanUtils.copyProperties(categoryDTO, command);
            command.setId(id);

            boolean result = foodCategoryService.updateCategory(command);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("更新食物分类失败，分类ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新食物分类失败"));
        }
    }

    /**
     * 删除食物分类
     */
    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteCategory(@PathVariable("id") Integer id) {
        try {
            boolean result = foodCategoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除食物分类失败，分类ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "删除食物分类失败"));
        }
    }

    /**
     * 添加食物
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> addFood(@RequestBody FoodSaveRequestDTO requestDTO) {
        try {
            // 创建命令对象
            FoodSaveCommand command = new FoodSaveCommand();
            BeanUtils.copyProperties(requestDTO, command);

            // 调用服务方法
            FoodItemDTO savedFood = foodService.saveFood(command);
            return ResponseEntity.ok(ApiResponse.success(savedFood));
        } catch (Exception e) {
            log.error("添加食物失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "添加食物失败"));
        }
    }

    /**
     * 更新食物
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemDTO>> updateFood(
            @PathVariable("id") Integer id,
            @RequestBody FoodUpdateRequestDTO requestDTO) {
        try {
            // 创建命令对象
            FoodUpdateCommand command = new FoodUpdateCommand();
            BeanUtils.copyProperties(requestDTO, command);
            command.setId(id);

            // 调用服务方法
            FoodItemDTO updatedFood = foodService.updateFood(command);
            return ResponseEntity.ok(ApiResponse.success(updatedFood));
        } catch (Exception e) {
            log.error("更新食物失败，食物ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新食物失败"));
        }
    }

    /**
     * 删除食物
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteFood(@PathVariable("id") Integer id) {
        try {
            boolean result = foodService.deleteFood(id);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除食物失败，食物ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "删除食物失败"));
        }
    }

    /**
     * 获取食物图片上传URL
     */
    @GetMapping("/upload-image-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getUploadImageUrl(
            @RequestParam("foodId") Long foodId,
            @RequestParam("contentType") String contentType) {
        try {
            // 生成上传URL
            String uploadUrl = fileService.generateUploadPresignedUrl(
                    foodId,
                    "foodimage",
                    contentType,
                    30); // 30分钟有效期

            return ResponseEntity.ok(ApiResponse.success(uploadUrl));
        } catch (Exception e) {
            log.error("获取食物图片上传URL失败，食物ID: {}", foodId, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取食物图片上传URL失败"));
        }
    }

    /**
     * 更新食物图片URL
     */
    @PutMapping("/{id}/image-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> updateFoodImageUrl(
            @PathVariable("id") Integer id,
            @RequestBody FoodImageUpdateRequestDTO requestDTO) {
        try {
            // 创建命令对象
            FoodImageUpdateCommand command = FoodImageUpdateCommand.withFoodId(id);
            command.setImageUrl(requestDTO.getImageUrl());

            // 调用服务方法
            boolean result = foodService.updateFoodImageUrl(command);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("更新食物图片URL失败，食物ID: {}", id, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新食物图片URL失败"));
        }
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

        try {
            Map<String, Object> result = foodService.batchImportFoods(foods);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (BusinessException e) {
            log.error("批量导入食物数据业务异常", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getCode(), "批量导入食物数据失败"));
        } catch (Exception e) {
            log.error("批量导入食物数据失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "批量导入食物数据失败"));
        }
    }
}
