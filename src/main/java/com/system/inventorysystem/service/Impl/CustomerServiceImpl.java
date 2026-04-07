package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.Customer;
import com.system.inventorysystem.exception.DuplicateCodeException;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.repository.CustomerRepository;
import com.system.inventorysystem.service.CustomerService;
import com.system.inventorysystem.strategy.discount.DiscountStrategyFactory;
import lombok.RequiredArgsConstructor;
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
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final com.system.inventorysystem.repository.ExportOrderRepository exportOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return customerRepository.searchCustomers(kw, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id).filter(c -> !c.isDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAllActive() {
        return customerRepository.findByStatusTrueAndDeletedFalse();
    }

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            if (customer.getCode() == null || customer.getCode().isBlank())
                customer.setCode(generateCode());
            if (customerRepository.existsByCodeAndDeletedFalse(customer.getCode()))
                throw new DuplicateCodeException("Mã khách hàng", customer.getCode());
        }
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(Long id, Customer incoming) {
        Customer existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khách hàng", id));
        existing.setCode(incoming.getCode());
        existing.setName(incoming.getName());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setAddress(incoming.getAddress());
        existing.setTaxCode(incoming.getTaxCode());
        existing.setCustomerType(incoming.getCustomerType());
        existing.setStatus(incoming.getStatus());
        return customerRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Customer c = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khách hàng", id));
        c.softDelete();
        customerRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCustomers() {
        return customerRepository.countByStatusTrueAndDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return customerRepository.existsByCodeAndDeletedFalse(code);
    }

    /**
     * Strategy Pattern — Tính giảm giá theo loại khách hàng.
     * Delegate toàn bộ lô-gic tính toán cho DiscountStrategy tương ứng.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Customer customer, BigDecimal totalAmount) {
        if (customer == null || totalAmount == null) return BigDecimal.ZERO;
        return discountStrategyFactory
                .getStrategy(customer.getCustomerType())
                .calculateDiscount(totalAmount);
    }

    private String generateCode() {
        return String.format("KH%04d", customerRepository.count() + 1);
    }
    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCustomerStats() {
        int year = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();

        long totalCustomers = customerRepository.countAllNonDeleted();
        long newCustomers = customerRepository.countNewCustomersByYearAndMonth(year, month);
        BigDecimal monthlyRevenue = exportOrderRepository.sumAmountByYearAndMonth(year, month);
        long returningCustomersCount = customerRepository.countReturningCustomers();

        double returnRate = 0.0;
        if (totalCustomers > 0) {
            returnRate = (double) returningCustomersCount / totalCustomers * 100.0;
        }

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCustomers", totalCustomers);
        stats.put("newCustomers", newCustomers);
        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        stats.put("returnRate", Math.round(returnRate));
        return stats;
    }
}
