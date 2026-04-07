package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.dto.GoogleLoginRequest;
import com.system.inventorysystem.dto.LoginRequest;
import com.system.inventorysystem.dto.UserInforFromProvider;
import com.system.inventorysystem.entity.AppUser;
import com.system.inventorysystem.enums.AuthType;
import com.system.inventorysystem.enums.UserRole;
import com.system.inventorysystem.exception.VerificationException;
import com.system.inventorysystem.service.AuthStrategy;
import com.system.inventorysystem.service.OauthVerifier;
import com.system.inventorysystem.service.UserService;
import com.system.inventorysystem.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class GoogleLoginStrategy implements AuthStrategy {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OauthVerifier oauthVerifier;
    public GoogleLoginStrategy(JwtUtil jwtUtil, UserService userService, OauthVerifier oauthVerifier){
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.oauthVerifier = oauthVerifier;
    }
    @Override
    public AuthResponse login(LoginRequest request) {
        GoogleLoginRequest googleLoginRequest = (GoogleLoginRequest) request;
        String idTokenString = googleLoginRequest.getGoogleAccessToken();
        UserInforFromProvider info = oauthVerifier.verify(idTokenString);
        String email = info.getEmail();
        String name = info.getName();
        AppUser user = userService.findUserByEmail(email);

        boolean isUserNoExist = false;
        // 2) Nếu user chưa tồn tại -> tạo mới
        if (user == null) {
            isUserNoExist = true;
            user = new AppUser();
            user.setEmail(email);
            user.setRole(UserRole.STAFF);
            user.setUsername(email);
            user.setPassword("DEFAULT");
            user.setFullName(name);
        }

        if (isUserNoExist) {
            user = this.userService.save(user);
        }

        String accessToken = this.jwtUtil.createAccessToken(email, user);
        String refreshToken = this.jwtUtil.createRefreshToken(email, user);

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
        return AuthType.GOOGLE;
    }
}
