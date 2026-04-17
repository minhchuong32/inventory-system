package com.system.inventorysystem.config;



import java.io.IOException;

import com.system.inventorysystem.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate =
            new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // Let Spring set 401 + WWW-Authenticate header
        delegate.commence(request, response, authException);

        response.setContentType("application/json;charset=UTF-8");

        String error =
                authException.getCause() != null
                        ? authException.getCause().getMessage()
                        : authException.getMessage();

        ErrorResponse<Object> res = new ErrorResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError(error);
        res.setMessage("Token không hợp lệ (hết hạn, sai định dạng, hoặc không truyền JWT)");

        mapper.writeValue(response.getWriter(), res);
    }
}