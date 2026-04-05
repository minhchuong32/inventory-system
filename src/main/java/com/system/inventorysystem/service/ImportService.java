package com.system.inventorysystem.service;

import com.system.inventorysystem.entity.ImportOrder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Optional;

public interface ImportService {
    Page<ImportOrder> findAll(String keyword, int page, int size);

    Optional<ImportOrder> findById(Long id);

    ImportOrder save(ImportOrder order);

    ImportOrder complete(Long id);

    void cancel(Long id);

    long countThisMonth();

    BigDecimal getTotalAmountThisMonth();
}
