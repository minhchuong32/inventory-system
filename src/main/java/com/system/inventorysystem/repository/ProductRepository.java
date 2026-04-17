// package com.system.inventorysystem.repository;

// import com.system.inventorysystem.entity.Product;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.util.List;

// @Repository
// public interface ProductRepository extends JpaRepository<Product, Long> {

//     @Query("SELECT p FROM Product p WHERE " +
//             "(:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//             "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//             "OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//             "AND p.status = true " +
//             "AND p.deleted = false " +
//             "ORDER BY p.id DESC")
//     Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

//     List<Product> findByStatusTrueAndDeletedFalse();

//     @Query("SELECT p FROM Product p WHERE p.status = true AND p.deleted = false AND (p.quantity IS NULL OR p.quantity <= p.minQuantity)")
//     List<Product> findLowStockProducts();

//     long countByStatusTrueAndDeletedFalse();

//     boolean existsByCodeAndDeletedFalse(String code);
// }


package com.system.inventorysystem.repository;
import com.system.inventorysystem.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusTrueAndDeletedFalse();
    boolean existsByCodeAndDeletedFalse(String code);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    long countByStatusTrueAndDeletedFalse();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = true AND " +
           "p.quantity <= p.minQuantity ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = true AND p.quantity = 0")
    List<Product> findOutOfStockProducts();

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.unit " +
           "WHERE p.deleted = false AND " +
           "(:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(p.code) LIKE LOWER(CONCAT('%',:kw,'%')))",
           countQuery = "SELECT COUNT(p) FROM Product p " +
           "WHERE p.deleted = false AND " +
           "(:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(p.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<Product> searchProducts(@Param("kw") String keyword, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.quantity * p.costPrice), 0) FROM Product p WHERE p.deleted = false AND p.status = true")
    java.math.BigDecimal getTotalStockValue();
}
