package com.system.inventorysystem.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NormalLoginRequest extends LoginRequest {
    private String username;
    private String password;
}