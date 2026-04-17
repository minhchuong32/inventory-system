package com.system.inventorysystem.service;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.dto.LoginRequest;
import com.system.inventorysystem.enums.AuthType;

public interface AuthStrategy {
    AuthResponse login(LoginRequest request);
    AuthType getAuthType();
}