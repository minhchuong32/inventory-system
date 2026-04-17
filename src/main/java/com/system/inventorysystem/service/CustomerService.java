package com.system.inventorysystem.service;

import com.system.inventorysystem.entity.Customer;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Page<Customer> findAll(String keyword, int page, int size);

    Optional<Customer> findById(Long id);

    List<Customer> findAllActive();

    Customer save(Customer customer);

    Customer update(Long id, Customer customer);

    void deleteById(Long id);

    long countActiveCustomers();

    boolean existsByCode(String code);

    /**
     * Tính số tiền giảm giá theo loại khách hàng.
     * Sử dụng Strategy Pattern — đọc customerType ở entity Customer.
     *
     * @param customer     khách hàng
     * @param totalAmount  tổng tiền trước giảm
     * @return số tiền được giảm
     */
    BigDecimal calculateDiscount(Customer customer, BigDecimal totalAmount);

    java.util.Map<String, Object> getCustomerStats();
}
