package com.system.inventorysystem.service;

import com.system.inventorysystem.dto.SupplierDTO;
import com.system.inventorysystem.entity.Supplier;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface SupplierService {
    Page<Supplier> findAll(String keyword, int page, int size);

    Optional<Supplier> findById(Long id);

    List<Supplier> findAllActive();

    Supplier save(Supplier supplier);

    Supplier update(Long id, Supplier supplier);

    void deleteById(Long id);

    long countActiveSuppliers();

    Page<SupplierDTO> findAllDTO(String keyword, int page, int size);
}
