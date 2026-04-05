package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.dto.LoginRequest;
import com.system.inventorysystem.dto.NormalLoginRequest;
import com.system.inventorysystem.entity.AppUser;
import com.system.inventorysystem.enums.AuthType;
import com.system.inventorysystem.service.AuthStrategy;
import com.system.inventorysystem.service.UserService;
import com.system.inventorysystem.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NormalLoginStrategy implements AuthStrategy {
    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;
    public NormalLoginStrategy(UserService userService, JwtUtil jwtUtil, AuthenticationManagerBuilder authenticationManagerBuilder){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        NormalLoginRequest normalLoginRequest = (NormalLoginRequest) request;
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                normalLoginRequest.getUsername(), normalLoginRequest.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUser user = this.userService.findByUserName(normalLoginRequest.getUsername());


        String accessToken = this.jwtUtil.createAccessToken(normalLoginRequest.getUsername(), user);
        String refreshToken = this.jwtUtil.createRefreshToken(normalLoginRequest.getUsername(), user);

        user.setRefreshToken(refreshToken);
        this.userService.updateUser(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.NORMAL;
    }
}
