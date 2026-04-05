package com.system.inventorysystem.controller;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.dto.LoginRequest;
import com.system.inventorysystem.service.Impl.AuthFacade;
import com.system.inventorysystem.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private AuthFacade authFacade;
    public AuthController(AuthFacade authFacade){
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authFacade.login(request);

        // Tạo 2 loại Cookie
        ResponseCookie accessCookie = JwtUtil.getAccessCookie(authResponse.getAccessToken());
        ResponseCookie refreshCookie = JwtUtil.getRefreshCookie(authResponse.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }
}