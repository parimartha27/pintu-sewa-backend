package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private CommonUtils commonUtils;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, Object>> validationErrors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, Object> errorDetail = new HashMap<>();
            errorDetail.put("field_error", fieldError.getField());
            errorDetail.put("error_message", List.of(Objects.requireNonNull(fieldError.getDefaultMessage())));
            validationErrors.add(errorDetail);
        }

        return commonUtils.setValidationErrorResponse(validationErrors);
    }
}
