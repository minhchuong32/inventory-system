package com.system.inventorysystem.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // Must match the parent
public class GoogleLoginRequest extends LoginRequest {
    private String googleAccessToken;
}