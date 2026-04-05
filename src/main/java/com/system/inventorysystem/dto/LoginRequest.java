package com.system.inventorysystem.dto;

import com.system.inventorysystem.enums.AuthType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "authType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NormalLoginRequest.class, name = "NORMAL"),
        @JsonSubTypes.Type(value = GoogleLoginRequest.class, name = "GOOGLE")
})
public abstract class LoginRequest {
    private AuthType authType;
}