package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler{

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

    @ExceptionHandler({EmailExistException.class})
    public ResponseEntity<ApiResponse> emailExistException(){
        return commonUtils.setResponse(
                "SIAP-SEWA-01-003",
                "Email has been registered. Please use other email",
                HttpStatus.CONFLICT,
                null);
    }

    @ExceptionHandler({PhoneNumberExistException.class})
    public ResponseEntity<ApiResponse> phoneNumberExistException(){
        return commonUtils.setResponse(
                "SIAP-SEWA-01-004",
                "Phone number has been registered. Please use other phone number",
                HttpStatus.CONFLICT,
                null);
    }
}
