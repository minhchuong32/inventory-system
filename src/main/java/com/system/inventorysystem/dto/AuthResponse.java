package com.system.inventorysystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.system.inventorysystem.enums.UserRole;
import com.system.inventorysystem.enums.AuthType;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String email;
    private String fullName;
    private UserRole role;
    @JsonIgnore
    private String refreshToken;
}