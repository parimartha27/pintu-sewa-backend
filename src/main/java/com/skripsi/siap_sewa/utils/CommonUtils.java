package com.skripsi.siap_sewa.utils;

import com.skripsi.siap_sewa.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {
    public ResponseEntity<ApiResponse> setResponse(HttpStatus httpStatus, String errorCode, String errorMessage, Object response){
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