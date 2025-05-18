package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;

/**
 * 食物更新请求DTO
 */
@Data
public class FoodUpdateRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;       // 食物ID，使用@JsonIgnore注解忽略该字段，因为ID已经在URL路径中提供

    private String name;      // 食物名称
    private String measure;   // 份量描述
    private Double grams;     // 克数
    private Double calories;  // 卡路里
    private Double protein;   // 蛋白质(g)
    private Double fat;       // 脂肪(g)
    private Double satFat;    // 饱和脂肪(g)
    private Double carbs;     // 碳水(g)
    private Integer categoryId; // 分类ID
    private String imageUrl;  // 图片URL

    @JsonIgnore
    private String category;  // 分类名称，前端需要但后端不处理，使用@JsonIgnore注解忽略

    @JsonIgnore
    private String categoryColor; // 分类颜色，前端需要但后端不处理，使用@JsonIgnore注解忽略

    @JsonIgnore
    private String remark;    // 备注，前端可能发送但后端不处理，使用@JsonIgnore注解忽略

    @JsonIgnore
    private String desc;      // 描述信息，前端需要但后端不处理，使用@JsonIgnore注解忽略

    @JsonIgnore
    private String unit;      // 单位，前端需要但后端不处理，使用@JsonIgnore注解忽略
}
