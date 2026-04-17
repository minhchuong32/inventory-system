package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.dto.SupplierDTO;
import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.exception.DuplicateCodeException;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.factory.SupplierFactory;
import com.system.inventorysystem.repository.SupplierRepository;
import com.system.inventorysystem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierFactory supplierFactory; // Factory Method

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return supplierRepository.searchSuppliers(kw, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id).filter(s -> !s.isDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> findAllActive() {
        return supplierRepository.findByStatusTrueAndDeletedFalse();
    }

    @Override
    public Supplier save(Supplier supplier) {
        if (supplier.getId() == null) {
            if (supplier.getCode() == null || supplier.getCode().isBlank())
                supplier.setCode(generateCode());
            if (supplierRepository.existsByCodeAndDeletedFalse(supplier.getCode()))
                throw new DuplicateCodeException("Mã nhà cung cấp", supplier.getCode());
        }
        supplierFactory.applyCreditLimit(supplier);
        log.info("[Factory] Saving supplier: code={}, type={}", supplier.getCode(), supplier.getSupplierType());
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier update(Long id, Supplier incoming) {
        Supplier existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("nhà cung cấp", id));
        supplierFactory.updateFromEntity(existing, incoming);
        log.info("[Factory] Updated supplier: id={}, code={}", id, existing.getCode());
        return supplierRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Supplier s = findById(id).orElseThrow(() -> new ResourceNotFoundException("nhà cung cấp", id));
        supplierRepository.delete(s);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveSuppliers() {
        return supplierRepository.countByStatusTrueAndDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> findAllDTO(String keyword, int page, int size) {
        return findAll(keyword, page, size).map(supplierFactory::toDTO);
    }

    @Override
    public void updateDebt(Supplier supplier, BigDecimal amount) {
        if (supplier.getCurrentDebt() == null) {
            supplier.setCurrentDebt(BigDecimal.ZERO);
        }
        supplier.setCurrentDebt(supplier.getCurrentDebt().add(amount));
        supplierRepository.save(supplier);
    }

    private String generateCode() {
        return String.format("NCC%04d", supplierRepository.count() + 1);
    }
}
