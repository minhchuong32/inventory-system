package com.system.inventorysystem.repository;

import com.system.inventorysystem.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByStatusTrueAndDeletedFalse();

    boolean existsByCodeAndDeletedFalse(String code);

    long countByStatusTrueAndDeletedFalse();

    @Query("SELECT c FROM Customer c WHERE c.deleted = false AND " +
            "(:kw IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%',:kw,'%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%',:kw,'%')) " +
            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<Customer> searchCustomers(@Param("kw") String keyword, Pageable pageable);
}
