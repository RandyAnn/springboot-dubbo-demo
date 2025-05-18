package com.example.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 营养统计请求DTO
 */
@Data
public class NutritionStatRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 日期，格式：yyyy-MM-dd
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确，应为yyyy-MM-dd")
    private String date;
    
    /**
     * 将字符串日期转换为LocalDate对象
     * @return LocalDate对象，如果date为null则返回当前日期
     */
    public LocalDate getDateAsLocalDate() {
        return date != null ? LocalDate.parse(date) : LocalDate.now();
    }
}
