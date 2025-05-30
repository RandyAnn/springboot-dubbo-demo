package com.example.common.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 营养素进度对比DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutrientProgressDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 上周摄入量
     */
    private Double lastWeek;
    
    /**
     * 本周摄入量
     */
    private Double thisWeek;
} 