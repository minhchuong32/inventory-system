package com.system.inventorysystem.repository;

import com.system.inventorysystem.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameAndDeletedFalse(String username);
    boolean existsByUsernameAndDeletedFalse(String username);
    Page<AppUser> findByDeletedFalse(Pageable pageable);
    long countByStatusTrueAndDeletedFalse();

    AppUser findByRefreshToken(String refreshToken);

    AppUser findByEmail(String email);
}
