package com.system.inventorysystem.config;

import java.io.IOException;

import com.system.inventorysystem.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{
    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }
   @Override
public void handle(HttpServletRequest request,
                   HttpServletResponse response,
                   AccessDeniedException ex)
        throws IOException, ServletException {

    // Nếu là request từ browser (HTML)
    String accept = request.getHeader("Accept");

    if (accept != null && accept.contains("text/html")) {
        request.getRequestDispatcher("/access-denied")
               .forward(request, response);
        return;
    }

    // Nếu là API → trả JSON
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType("application/json;charset=UTF-8");

    ErrorResponse<Object> res = new ErrorResponse<>();
    res.setStatusCode(HttpStatus.FORBIDDEN.value());
    res.setError(ex.getMessage());
    res.setMessage("Bạn không có quyền truy cập tài nguyên này");

    mapper.writeValue(response.getWriter(), res);
}

}