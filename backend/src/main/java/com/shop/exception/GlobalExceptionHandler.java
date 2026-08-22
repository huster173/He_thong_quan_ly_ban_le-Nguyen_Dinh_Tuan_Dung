package com.shop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getCode().getStatus())
                .body(body(ex.getCode().name(), ex.getMessage(), ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage() == null ? "khong hop le" : f.getDefaultMessage(),
                        (a, b) -> a));

        ErrorCode code = errorCodeFor(fields.keySet());
        return ResponseEntity.status(code.getStatus())
                .body(body(code.name(), "Du lieu don hang khong hop le.", fields));
    }

    private ErrorCode errorCodeFor(Set<String> invalidFields) {
        if (invalidFields.contains("items")) {
            return ErrorCode.EMPTY_ORDER;
        }
        if (invalidFields.stream().anyMatch(field -> field.endsWith("productId"))) {
            return ErrorCode.INVALID_PRODUCT_ID;
        }
        return ErrorCode.INVALID_QUANTITY;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(body(
                "INVALID_PARAMETER",
                "Tham so '" + ex.getName() + "' khong hop le.",
                Map.of("parameter", ex.getName(), "value", String.valueOf(ex.getValue()))));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof BusinessException business) {
                return handleBusiness(business);
            }
            cause = cause.getCause();
        }
        return ResponseEntity.badRequest()
                .body(body("MALFORMED_REQUEST", "Body request khong doc duoc.", Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Loi khong luong truoc", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(ErrorCode.INTERNAL_ERROR.name(),
                        "He thong dang gap su co, vui long thu lai.", Map.of()));
    }

    private Map<String, Object> body(String code, String message, Map<String, Object> details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        payload.put("details", details == null ? Map.of() : details);
        return payload;
    }
}
