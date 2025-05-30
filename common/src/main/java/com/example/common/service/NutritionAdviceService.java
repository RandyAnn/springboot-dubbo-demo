package com.example.common.service;

import com.example.common.command.nutrition.NutritionAdviceManageCommand;
import com.example.common.dto.nutrition.NutritionAdviceDisplayDTO;
import com.example.common.dto.nutrition.NutritionAdviceResponseDTO;
import com.example.common.entity.NutritionAdvice;

import java.util.List;

/**
 * 营养建议服务接口
 */
public interface NutritionAdviceService {

    /**
     * 获取所有营养建议
     * @return 营养建议列表
     */
    List<NutritionAdviceResponseDTO> getAllAdvices();

    /**
     * 根据ID获取营养建议
     * @param id 营养建议ID
     * @return 营养建议
     */
    NutritionAdviceResponseDTO getAdviceById(Long id);

    /**
     * 创建营养建议
     * @param command 营养建议命令对象
     * @return 创建后的营养建议响应DTO
     */
    NutritionAdviceResponseDTO createAdvice(NutritionAdviceManageCommand command);

    /**
     * 更新营养建议
     * @param command 营养建议命令对象（包含ID）
     * @return 更新后的营养建议响应DTO
     */
    NutritionAdviceResponseDTO updateAdvice(NutritionAdviceManageCommand command);

    /**
     * 删除营养建议
     * @param id 营养建议ID
     * @return 是否删除成功
     */
    boolean deleteAdvice(Long id);

    /**
     * 根据条件类型和百分比获取适用的营养建议
     * @param conditionType 条件类型
     * @param percentage 百分比值
     * @return 营养建议
     */
    NutritionAdviceResponseDTO getAdviceByCondition(String conditionType, Integer percentage);

    /**
     * 获取默认营养建议
     * @return 默认营养建议
     */
    NutritionAdviceResponseDTO getDefaultAdvice();

    /**
     * 根据条件类型获取所有营养建议
     * @param conditionType 条件类型
     * @return 营养建议列表
     */
    List<NutritionAdviceResponseDTO> getAdvicesByConditionType(String conditionType);

    /**
     * 将实体转换为DTO
     * @param advice 营养建议实体
     * @return 营养建议DTO
     */
    NutritionAdviceDisplayDTO convertToDTO(NutritionAdvice advice);

    /**
     * 将实体转换为响应DTO
     * @param advice 营养建议实体
     * @return 营养建议响应DTO
     */
    NutritionAdviceResponseDTO convertToResponseDTO(NutritionAdvice advice);
}
