package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private CommonUtils commonUtils;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

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
    public ResponseEntity<ApiResponse> handleEmailExistException(EmailExistException ex) {
        log.warn("Email conflict: {}", ex.getMessage());
        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.CONFLICT,
                null
        );
    }

    @ExceptionHandler({PhoneNumberExistException.class})
    public ResponseEntity<ApiResponse> handlePhoneNumberExistException(PhoneNumberExistException ex) {
        log.warn("Phone number conflict: {}", ex.getMessage());
        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.CONFLICT,
                null
        );
    }

    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<ApiResponse> handleDataNotFoundException(DataNotFoundException ex) {
        log.warn("Data not found: {}", ex.getMessage());
        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAllExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
    }

    @ExceptionHandler(CheckoutValidationException.class)
    public ResponseEntity<ApiResponse> handleCheckoutValidationException(CheckoutValidationException ex) {
        log.warn("Checkout validation failed: {}", ex.getMessage());
        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                null
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse> handleInsufficientStockException(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("availableStock", ex.getAvailableStock());
        details.put("requestedQuantity", ex.getRequestedQuantity());

        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                details
        );
    }

    @ExceptionHandler(MinimumRentNotMetException.class)
    public ResponseEntity<ApiResponse> handleMinimumRentNotMetException(MinimumRentNotMetException ex) {
        log.warn("Minimum rent not met: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("minimumRent", ex.getMinRent());
        details.put("requestedQuantity", ex.getRequestedQuantity());

        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                details
        );
    }

    @ExceptionHandler(CheckoutProcessingException.class)
    public ResponseEntity<ApiResponse> handleCheckoutProcessingException(CheckoutProcessingException ex) {
        log.error("Checkout processing failed for product {}: {}", ex.getProductId(), ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("productId", ex.getProductId());

        return commonUtils.setResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                details
        );
    }
}
