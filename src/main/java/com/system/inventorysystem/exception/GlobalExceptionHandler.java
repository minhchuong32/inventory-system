package com.system.inventorysystem.exception;

import com.system.inventorysystem.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request, RedirectAttributes ra) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        if (isApiRequest(request)) {
            ErrorResponse<Object> res = ErrorResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .error("Business Exception")
                    .message(ex.getMessage())
                    .build();
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(AuthenticationException.class)
    public Object handleAuthenticationException(AuthenticationException ex, HttpServletRequest request, RedirectAttributes ra) {
        log.warn("Authentication exception: {}", ex.getMessage());
        if (isApiRequest(request)) {
            ErrorResponse<Object> res = ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Tên đăng nhập hoặc mật khẩu không chính xác.")
                    .build();
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        ra.addFlashAttribute("error", "Đăng nhập không thành công: " + ex.getMessage());
        return "redirect:/auth/login";
    }

    // KHÔNG khai báo @ExceptionHandler(Exception.class) vì nó sẽ bắt luôn
    // AccessDeniedException và nuốt mất trước khi Spring Security xử lý.
    // Spring Security's ExceptionTranslationFilter nằm trong filter chain,
    // TRƯỚC DispatcherServlet — nên nếu @ControllerAdvice bắt AccessDeniedException
    // thì exception không bao giờ thoát ra được filter chain để redirect /access-denied.
    //
    // Giải pháp: chỉ bắt các RuntimeException cụ thể, KHÔNG bắt Exception.class chung.
    @ExceptionHandler({
        RuntimeException.class,
        IllegalArgumentException.class,
        IllegalStateException.class,
        AuthException.class
    })
    public Object handleRuntime(RuntimeException ex, HttpServletRequest request, RedirectAttributes ra) {
        // Nếu là AccessDeniedException (subclass của RuntimeException) → ném lại
        // để Spring Security filter xử lý redirect sang /access-denied
        if (ex instanceof AccessDeniedException) {
            throw ex;
        }
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        if (isApiRequest(request)) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorName = "Internal Server Error";
            if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
                status = HttpStatus.BAD_REQUEST;
                errorName = "Bad Request";
            }
            ErrorResponse<Object> res = ErrorResponse.builder()
                    .statusCode(status.value())
                    .error(errorName)
                    .message(ex.getMessage())
                    .build();
            return new ResponseEntity<>(res, status);
        }
        ra.addFlashAttribute("error", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/dashboard";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }
}