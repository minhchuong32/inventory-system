package com.system.inventorysystem.factory;

import com.system.inventorysystem.dto.UserDTO;
import com.system.inventorysystem.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFactory implements EntityFactory<AppUser, UserDTO> {

    private final PasswordEncoder passwordEncoder;

    public AppUser prepareForCreate(AppUser user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return user;
    }

    public AppUser updateFromEntity(AppUser existing, AppUser incoming, boolean changePassword) {
        existing.setFullName(incoming.getFullName());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setRole(incoming.getRole());
        existing.setStatus(incoming.getStatus());

        if (changePassword && incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
        }

        return existing;
    }

    @Override
    public UserDTO toDTO(AppUser user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .roleDisplayName(user.getRole().getDisplayName())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
