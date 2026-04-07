package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.dto.LoginRequest;
import com.system.inventorysystem.entity.AppUser;
import com.system.inventorysystem.repository.AppUserRepository;
import com.system.inventorysystem.service.AuthStrategy;
import com.system.inventorysystem.service.UserService;
import com.system.inventorysystem.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthFacade {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private AuthStrategyFactory factory;
    public AuthFacade(UserService userService, JwtUtil jwtUtil, AuthStrategyFactory factory){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.factory = factory;
    }
    public AuthResponse login(LoginRequest request) {
        AuthStrategy strategy =
                factory.getStrategy(request.getAuthType());
        return strategy.login(request);
    }

    public AuthResponse refreshToken(String refreshToken) {
        AppUser user = this.userService.findUserByRefreshToken(refreshToken);

        String accessToken = this.jwtUtil.createAccessToken(user.getUsername(), user);
        String newRefreshToken = this.jwtUtil.createRefreshToken(user.getUsername(), user);
        user.setRefreshToken(newRefreshToken);
        this.userService.updateUser(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}