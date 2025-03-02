package com.skripsi.siap_sewa.utils;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CommonUtils {
    public ResponseEntity<ApiResponse> setResponse(ErrorMessageEnum errorMessageEnum, Object response){
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(errorMessageEnum.getErrorCode())
                                .errorMessage(errorMessageEnum.getErrorMessage())
                                .build()
                        )
                        .outputSchema(response)
                        .build(),
                errorMessageEnum.getHttpStatus());
    }

    public ResponseEntity<ApiResponse> setValidationErrorResponse(List<Map<String, Object>> validationErrors) {
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(Constant.BAD_REQUEST_CODE)
                                .errorMessage("Request invalid")
                                .build()
                        )
                        .outputSchema(validationErrors)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ApiResponse> setResponse(String errorCode, String errorMessage, HttpStatus httpStatus, Object response){
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build()
                        )
                        .outputSchema(response)
                        .build(),
                httpStatus);
    }
}