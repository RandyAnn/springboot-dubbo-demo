package com.example.nutrition.service;

import com.example.common.command.NutritionAdviceManageCommand;
import com.example.common.dto.NutritionAdviceDTO;
import com.example.common.dto.NutritionAdviceResponseDTO;
import com.example.common.entity.NutritionAdvice;
import com.example.common.service.NutritionAdviceService;
import com.example.nutrition.mapper.NutritionAdviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 营养建议服务实现类
 */
@Slf4j
@Service
@DubboService
public class NutritionAdviceServiceImpl implements NutritionAdviceService {

    @Autowired
    private NutritionAdviceMapper nutritionAdviceMapper;

    @Override
    public List<NutritionAdvice> getAllAdvices() {
        return nutritionAdviceMapper.selectList(null);
    }

    @Override
    public NutritionAdvice getAdviceById(Long id) {
        return nutritionAdviceMapper.selectById(id);
    }

    @Override
    public NutritionAdviceResponseDTO createAdvice(NutritionAdviceManageCommand command) {
        NutritionAdvice advice = new NutritionAdvice();
        advice.setType(command.getType());
        advice.setTitle(command.getTitle());
        advice.setDescription(command.getDescription());
        advice.setConditionType(command.getConditionType());
        advice.setMinPercentage(command.getMinPercentage());
        advice.setMaxPercentage(command.getMaxPercentage());
        advice.setIsDefault(command.getIsDefault());
        advice.setPriority(command.getPriority());
        advice.setStatus(command.getStatus());

        nutritionAdviceMapper.insert(advice);
        return convertToResponseDTO(advice);
    }

    @Override
    public NutritionAdviceResponseDTO updateAdvice(Long id, NutritionAdviceManageCommand command) {
        NutritionAdvice advice = nutritionAdviceMapper.selectById(id);
        if (advice == null) {
            return null;
        }

        advice.setType(command.getType());
        advice.setTitle(command.getTitle());
        advice.setDescription(command.getDescription());
        advice.setConditionType(command.getConditionType());
        advice.setMinPercentage(command.getMinPercentage());
        advice.setMaxPercentage(command.getMaxPercentage());
        advice.setIsDefault(command.getIsDefault());
        advice.setPriority(command.getPriority());
        advice.setStatus(command.getStatus());

        nutritionAdviceMapper.updateById(advice);
        return convertToResponseDTO(advice);
    }



    @Override
    public boolean deleteAdvice(Long id) {
        return nutritionAdviceMapper.deleteById(id) > 0;
    }

    @Override
    public NutritionAdvice getAdviceByCondition(String conditionType, Integer percentage) {
        List<NutritionAdvice> advices = nutritionAdviceMapper.findByConditionTypeAndPercentage(conditionType, percentage);
        return advices.isEmpty() ? null : advices.get(0);
    }

    @Override
    public NutritionAdvice getDefaultAdvice() {
        return nutritionAdviceMapper.findDefaultAdvice();
    }

    @Override
    public List<NutritionAdvice> getAdvicesByConditionType(String conditionType) {
        return nutritionAdviceMapper.findByConditionType(conditionType);
    }

    @Override
    public NutritionAdviceDTO convertToDTO(NutritionAdvice advice) {
        if (advice == null) {
            return null;
        }

        NutritionAdviceDTO dto = new NutritionAdviceDTO();
        dto.setType(advice.getType());
        dto.setTitle(advice.getTitle());
        dto.setDescription(advice.getDescription());

        return dto;
    }

    @Override
    public NutritionAdviceResponseDTO convertToResponseDTO(NutritionAdvice advice) {
        if (advice == null) {
            return null;
        }

        NutritionAdviceResponseDTO dto = new NutritionAdviceResponseDTO();
        dto.setId(advice.getId());
        dto.setType(advice.getType());
        dto.setTitle(advice.getTitle());
        dto.setDescription(advice.getDescription());
        dto.setConditionType(advice.getConditionType());
        dto.setMinPercentage(advice.getMinPercentage());
        dto.setMaxPercentage(advice.getMaxPercentage());
        dto.setIsDefault(advice.getIsDefault());
        dto.setPriority(advice.getPriority());
        dto.setStatus(advice.getStatus());
        dto.setCreatedAt(advice.getCreatedAt());
        dto.setUpdatedAt(advice.getUpdatedAt());

        return dto;
    }
}
