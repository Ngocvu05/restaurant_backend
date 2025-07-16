package com.management.restaurant.service.implement;

import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.mapper.PreOrderMapper;
import com.management.restaurant.model.PreOrder;
import com.management.restaurant.repository.PreorderRepository;
import com.management.restaurant.service.PreOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreOrderServiceImpl implements PreOrderService {
    private final PreorderRepository preorderRepository;
    private final PreOrderMapper preOrderMapper;

    @Override
    public List<PreOrderDTO> getAll() {
        return preorderRepository.findAll().stream()
                .map(preOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PreOrderDTO getById(Long id) {
        PreOrder preorder = preorderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreOrder not found"));
        return preOrderMapper.toDTO(preorder);
    }

    @Override
    public PreOrderDTO create(PreOrderDTO dto) {
        PreOrder preorder = preOrderMapper.toEntity(dto);
        return preOrderMapper.toDTO(preorderRepository.save(preorder));
    }

    @Override
    public PreOrderDTO update(Long id, PreOrderDTO dto) {
        PreOrder existing = preorderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreOrder not found"));
        existing.setQuantity(dto.getQuantity());
        existing.setNote(dto.getNote());
        return preOrderMapper.toDTO(preorderRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        preorderRepository.deleteById(id);
    }
}
