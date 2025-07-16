package com.management.restaurant.service;

import com.management.restaurant.dto.PreOrderDTO;

import java.util.List;

public interface PreOrderService {
    List<PreOrderDTO> getAll();

    PreOrderDTO getById(Long id);

    PreOrderDTO create(PreOrderDTO dto);

    PreOrderDTO update(Long id, PreOrderDTO dto);

    void delete(Long id);
}
