package com.system.inventorysystem.config;

import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Role hierarchy:
 * ADMIN – toàn quyền (xem + tạo + xác nhận + xóa + quản lý user)
 * MANAGER – quản lý nghiệp vụ (xem + tạo + xác nhận, KHÔNG xóa sản phẩm/NCC/KH,
 * KHÔNG quản lý user)
 * STAFF – nhân viên nhập liệu (chỉ xem danh sách + tạo phiếu, KHÔNG xác
 * nhận/hủy/xóa)
 */

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler, JwtTokenFilter jwtTokenFilter) throws Exception {
        http
                .csrf(c -> c.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers("/suppliers", "/auth/**", "/api/auth/**", "/users", "/search",
                                        "/search/**",
                                        "/search/room-type/filter", "/search/room-type/filter/**",
                            "/payments/call-back", "/health")
                                .permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                                .requestMatchers("/error", "/access-denied").permitAll()

                                // ── ADMIN only ────────────────────────────────────
                                .requestMatchers("/users/**").hasRole("ADMIN")

                                // ── ADMIN + MANAGER: xóa/điều chỉnh master data ──
                                .requestMatchers("/products/*/delete").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers("/suppliers/*/delete").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers("/customers/*/delete").hasAnyRole("ADMIN", "MANAGER")

                                // ── ADMIN + MANAGER: xác nhận / hủy phiếu ────────
                                .requestMatchers("/imports/*/complete", "/imports/*/cancel")
                                .hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers("/exports/*/complete", "/exports/*/cancel")
                                .hasAnyRole("ADMIN", "MANAGER")
                                .anyRequest().authenticated())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(customAuthenticationEntryPoint) // 401)
                                .accessDeniedHandler(customAccessDeniedHandler) // 403
                )
                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // @Bean
    // public JwtAuthenticationConverter jwtAuthenticationConverter() {
    // JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
    // JwtGrantedAuthoritiesConverter();
    // grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
    // grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
    //
    // JwtAuthenticationConverter jwtAuthenticationConverter = new
    // JwtAuthenticationConverter();
    // jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    // return jwtAuthenticationConverter;
    // }
    //
    // @Bean
    // public JwtEncoder jwtEncoder() {
    // return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    // }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKey()).build();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, MacAlgorithm.HS256.getName());
    }
}
