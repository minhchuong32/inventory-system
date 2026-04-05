package com.system.inventorysystem.repository;
import com.system.inventorysystem.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Long> {
    List<WareHouse> findByStatusTrueAndDeletedFalse();
    boolean existsByCodeAndDeletedFalse(String code);
}
