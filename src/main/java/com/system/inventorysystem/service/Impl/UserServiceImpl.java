package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.AppUser;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.factory.UserFactory;
import com.system.inventorysystem.repository.AppUserRepository;
import com.system.inventorysystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final UserFactory userFactory;

    @Override
    @Transactional(readOnly = true)
    public Page<AppUser> findAll(int page, int size) {
        return userRepository.findByDeletedFalse(
                PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id).filter(u -> !u.isDeleted());
    }

    @Override
    public AppUser findByUserName(String userName) {
        return this.userRepository.findByUsernameAndDeletedFalse(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public AppUser save(AppUser user) {
        if (userRepository.existsByUsernameAndDeletedFalse(user.getUsername()))
            throw new RuntimeException("Tên đăng nhập '" + user.getUsername() + "' đã tồn tại");
        AppUser prepared = userFactory.prepareForCreate(user);
        return userRepository.save(prepared);
    }

    @Override
    public AppUser update(Long id, AppUser incoming, boolean changePassword) {
        AppUser existing = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        AppUser updated = userFactory.updateFromEntity(existing, incoming, changePassword);
        return userRepository.save(updated);
    }

    @Override
    public void toggleStatus(Long id) {
        AppUser user = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        user.setStatus(!Boolean.TRUE.equals(user.getStatus()));
        userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        AppUser user = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("người dùng", id));
        user.softDelete();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameAndDeletedFalse(username);
    }

    @Override
    public void updateUser(AppUser user) {
        userRepository.save(user);
    }

    @Override
    public AppUser findUserByRefreshToken(String refreshToken) {
        AppUser user = this.userRepository.findByRefreshToken(refreshToken);
        if (user == null) {
            throw new EntityNotFoundException("Refresh Token not found");
        }
        return user;
    }

    @Override
    public AppUser findUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }
}
