package com.system.inventorysystem.service;

import com.system.inventorysystem.entity.AppUser;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserService {
    Page<AppUser> findAll(int page, int size);
    Optional<AppUser> findById(Long id);
    AppUser findByUserName(String userName);
    AppUser save(AppUser user);
    AppUser update(Long id, AppUser incoming, boolean changePassword);
    void toggleStatus(Long id);
    void deleteById(Long id);
    boolean existsByUsername(String username);

    void updateUser(AppUser user);

    AppUser findUserByRefreshToken(String refreshToken);

    AppUser findUserByEmail(String email);
}
