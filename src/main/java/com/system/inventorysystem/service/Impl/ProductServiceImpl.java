package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.entity.StockMovement;
import com.system.inventorysystem.enums.MovementType;
import com.system.inventorysystem.exception.DuplicateCodeException;
import com.system.inventorysystem.exception.InsufficientStockException;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.repository.ProductRepository;
import com.system.inventorysystem.repository.StockMovementRepository;
import com.system.inventorysystem.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return productRepository.searchProducts(kw, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).filter(p -> !p.isDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        return productRepository.findByStatusTrueAndDeletedFalse();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            // Generate code if not provided
            if (product.getCode() == null || product.getCode().isBlank()) {
                product.setCode(generateCode());
            }
            // Check duplicate code
            if (productRepository.existsByCodeAndDeletedFalse(product.getCode())) {
                throw new DuplicateCodeException("Mã sản phẩm", product.getCode());
            }
            // Fallback for barcode to avoid UNIQUE NULL constraint in DB
            if (product.getBarcode() == null || product.getBarcode().isBlank()) {
                product.setBarcode(product.getCode());
            }
        }
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product incoming) {
        Product existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", id));

        // Update fields
        existing.setCode(incoming.getCode());
        existing.setName(incoming.getName());

        // Fallback for barcode to avoid UNIQUE NULL constraint in DB
        if (incoming.getBarcode() == null || incoming.getBarcode().isBlank()) {
            existing.setBarcode(incoming.getCode());
        } else {
            existing.setBarcode(incoming.getBarcode());
        }
        existing.setCategory(incoming.getCategory());
        existing.setSupplier(incoming.getSupplier());
        existing.setUnit(incoming.getUnit());
        existing.setWarehouse(incoming.getWarehouse());
        existing.setCostPrice(incoming.getCostPrice());
        existing.setSellPrice(incoming.getSellPrice());
        existing.setMinQuantity(incoming.getMinQuantity());
        existing.setMaxQuantity(incoming.getMaxQuantity());
        existing.setWeight(incoming.getWeight());
        existing.setDescription(incoming.getDescription());

        // Only update image if provided
        if (incoming.getImageUrl() != null && !incoming.getImageUrl().isBlank()) {
            existing.setImageUrl(incoming.getImageUrl());
        }

        existing.setStatus(incoming.getStatus());

        return productRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Product p = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", id));
        p.softDelete();
        productRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countByStatusTrueAndDeletedFalse();
    }

    @Override
    public void adjustStock(Long productId, int delta, String reason, String refCode) {
        Product product = findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", productId));

        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
        int newQty = currentQty + delta;

        if (newQty < 0) {
            throw new InsufficientStockException(product.getName(), Math.abs(delta), currentQty);
        }

        product.setQuantity(newQty);
        productRepository.save(product);

        // Record movement
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(delta > 0 ? MovementType.IN : MovementType.OUT)
                .quantity(Math.abs(delta))
                .beforeQuantity(currentQty)
                .afterQuantity(newQty)
                .referenceCode(refCode)
                .note(reason)
                .build();

        stockMovementRepository.save(movement);
    }

    private String generateCode() {
        long count = productRepository.count();
        return String.format("SP%05d", count + 1);
    }
}
