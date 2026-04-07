package com.system.inventorysystem.config;

import com.system.inventorysystem.dto.AuthResponse;
import com.system.inventorysystem.service.Impl.AuthFacade;
import com.system.inventorysystem.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthFacade authFacade;
    public JwtTokenFilter(JwtUtil jwtUtil,AuthFacade authFacade) {
        this.jwtUtil = jwtUtil;
        this.authFacade = authFacade;
    }



    private String getJwtFromCookie(HttpServletRequest request,String cookieType) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieType.equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromCookie(request, "access-token");

        try {
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                processAuthentication(request, token);
            }
        } catch (ExpiredJwtException e) {
            // ACCESS TOKEN HẾT HẠN -> Thử dùng Refresh Token
            String refreshToken = getJwtFromCookie(request, "refresh-token");

            if (StringUtils.hasText(refreshToken)) {
                try {
                    // Logic làm mới token từ database/service
                    AuthResponse newTokens = authFacade.refreshToken(refreshToken);

                    // Ghi đè Cookie mới vào Response
                    // Lưu ý: JwtUtil.getAccessCookie nên trả về chuỗi Set-Cookie (String)
                    response.addHeader(HttpHeaders.SET_COOKIE, JwtUtil.getAccessCookie(newTokens.getAccessToken()).toString());
                    response.addHeader(HttpHeaders.SET_COOKIE, JwtUtil.getRefreshCookie(newTokens.getRefreshToken()).toString());

                    // Tiếp tục xác thực với token mới để request hiện tại không bị lỗi 401
                    processAuthentication(request, newTokens.getAccessToken());
                } catch (Exception ex) {
                    // Nếu refresh fail (token lậu, bị thu hồi...), cho đi tiếp để Security chặn ở entry point
                    logger.error("Could not refresh token", ex);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    // Tách logic xác thực ra method riêng để tái sử dụng
    private void processAuthentication(HttpServletRequest request, String token) {
        Claims claims = jwtUtil.getClaims(token);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}