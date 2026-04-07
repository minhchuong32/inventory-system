package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.entity.StockMovement;
import com.system.inventorysystem.entity.*;
import com.system.inventorysystem.enums.MovementType;
import com.system.inventorysystem.repository.*;
import com.system.inventorysystem.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class StockServiceImpl implements StockService {
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WareHouseRepository warehouseRepository;

    @Override
    public void recordMovement(Long productId, Long warehouseId, MovementType type,
                               int qty, int before, int after,
                               String refCode, String refType, String note) {
        Product product = productRepository.findById(productId).orElse(null);
        WareHouse warehouse = warehouseId != null ? warehouseRepository.findById(warehouseId).orElse(null) : null;

        StockMovement movement = StockMovement.builder()
            .product(product)
            .warehouse(warehouse)
            .movementType(type)
            .quantity(qty)
            .beforeQuantity(before)
            .afterQuantity(after)
            .referenceCode(refCode)
            .referenceType(refType)
            .note(note)
            .build();
        movementRepository.save(movement);
    }

    @Override @Transactional(readOnly = true)
    public Page<StockMovement> getMovementHistory(Long productId, int page, int size) {
        return movementRepository.findByProductIdAndDeletedFalseOrderByCreatedAtDesc(
            productId, PageRequest.of(page, size));
    }
}
