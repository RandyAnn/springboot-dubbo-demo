package com.example.common.dto.nutrition;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 营养趋势请求DTO
 */
@Data
public class NutritionTrendRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 开始日期，格式：yyyy-MM-dd
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "开始日期格式不正确，应为yyyy-MM-dd")
    private String startDate;
    
    /**
     * 结束日期，格式：yyyy-MM-dd
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "结束日期格式不正确，应为yyyy-MM-dd")
    private String endDate;
    
    /**
     * 类型：week、month、custom
     */
    @Pattern(regexp = "^(week|month|custom)$", message = "类型只能是week、month或custom")
    private String type = "week";
    
    /**
     * 将字符串开始日期转换为LocalDate对象
     * @return LocalDate对象，如果startDate为null则根据type自动计算
     */
    public LocalDate getStartDateAsLocalDate() {
        if (startDate != null) {
            return LocalDate.parse(startDate);
        }
        
        LocalDate now = LocalDate.now();
        switch (type) {
            case "week":
                return now.minusDays(6); // 最近一周
            case "month":
                return now.minusDays(29); // 最近一个月
            default:
                return now.minusDays(6); // 默认一周
        }
    }
    
    /**
     * 将字符串结束日期转换为LocalDate对象
     * @return LocalDate对象，如果endDate为null则返回当前日期
     */
    public LocalDate getEndDateAsLocalDate() {
        return endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
    }
}
