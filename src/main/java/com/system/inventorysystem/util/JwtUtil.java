package com.system.inventorysystem.util;

import com.system.inventorysystem.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private static String secret;
    private static long jwtAccessTokenExpiration;
    private static long jwtRefreshTokenExpiration;

    @Value("${jwt.base64-secret}")
    public void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    @Value("${jwt.access-token-validity-in-seconds}")
    public void setJwtAccessTokenExpiration(long expiration) {
        JwtUtil.jwtAccessTokenExpiration = expiration;
    }

    @Value("${jwt.refresh-token-validity-in-seconds}")
    public void setJwtRefreshTokenExpiration(long expiration) {
        JwtUtil.jwtRefreshTokenExpiration = expiration;
    }

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // --- GENERATE TOKENS ---

    public String createAccessToken(String userName, AppUser user) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessTokenExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userName, AppUser user) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshTokenExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- VALIDATE & EXTRACT ---

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- COOKIE HELPERS ---

    public static ResponseCookie getAccessCookie(String accessToken) {
        return ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(false) // Set true if using HTTPS
                .path("/")
                .maxAge(jwtAccessTokenExpiration)
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie getRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtRefreshTokenExpiration)
                .sameSite("Lax")
                .build();
    }

    // --- CONTEXT HELPERS ---

    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }
}