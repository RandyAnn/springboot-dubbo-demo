package com.example.nutrition.service;

import com.example.common.command.nutrition.NutritionAdviceManageCommand;
import com.example.common.dto.nutrition.NutritionAdviceDisplayDTO;
import com.example.common.dto.nutrition.NutritionAdviceResponseDTO;
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
    public List<NutritionAdviceResponseDTO> getAllAdvices() {
        List<NutritionAdvice> advices = nutritionAdviceMapper.selectList(null);
        return advices.stream()
                .map(this::convertToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public NutritionAdviceResponseDTO getAdviceById(Long id) {
        NutritionAdvice advice = nutritionAdviceMapper.selectById(id);
        return convertToResponseDTO(advice);
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
    public NutritionAdviceResponseDTO updateAdvice(NutritionAdviceManageCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("更新营养建议时ID不能为空");
        }

        NutritionAdvice advice = nutritionAdviceMapper.selectById(command.getId());
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
    public NutritionAdviceResponseDTO getAdviceByCondition(String conditionType, Integer percentage) {
        List<NutritionAdvice> advices = nutritionAdviceMapper.findByConditionTypeAndPercentage(conditionType, percentage);
        if (advices.isEmpty()) {
            return null;
        }
        return convertToResponseDTO(advices.get(0));
    }

    @Override
    public NutritionAdviceResponseDTO getDefaultAdvice() {
        NutritionAdvice advice = nutritionAdviceMapper.findDefaultAdvice();
        return convertToResponseDTO(advice);
    }

    @Override
    public List<NutritionAdviceResponseDTO> getAdvicesByConditionType(String conditionType) {
        List<NutritionAdvice> advices = nutritionAdviceMapper.findByConditionType(conditionType);
        return advices.stream()
                .map(this::convertToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public NutritionAdviceDisplayDTO convertToDTO(NutritionAdvice advice) {
        if (advice == null) {
            return null;
        }

        NutritionAdviceDisplayDTO dto = new NutritionAdviceDisplayDTO();
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
