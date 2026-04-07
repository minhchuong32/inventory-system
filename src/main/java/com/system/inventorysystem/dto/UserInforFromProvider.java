package com.system.inventorysystem.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInforFromProvider {
    private String email;
    private String name;
}
