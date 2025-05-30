package com.example.common.dto.food;

import lombok.Data;
import java.io.Serializable;

/**
 * 食物图片更新请求DTO
 */
@Data
public class FoodImageUpdateRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String imageUrl;  // 图片URL
}
