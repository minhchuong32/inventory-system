package com.system.inventorysystem.entity;

import com.system.inventorysystem.enums.AuthType;
import com.system.inventorysystem.enums.UserRole;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.system.inventorysystem.entity.base.BaseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends BaseEntity {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.STAFF;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 20, nullable = true)
    @Builder.Default
    private AuthType authType = AuthType.NORMAL;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isManager() {
        return role == UserRole.MANAGER || isAdmin();
    }

    public void recordLogin() {
        this.lastLogin = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @Override
    public boolean isValid() {
        return super.isValid() && username != null && !username.isBlank();
    }
}
