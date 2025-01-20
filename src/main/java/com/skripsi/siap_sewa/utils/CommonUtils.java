package com.skripsi.siap_sewa.utils;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
}