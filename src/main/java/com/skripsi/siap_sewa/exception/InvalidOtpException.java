package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class InvalidOtpException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.INVALID_OTP.getErrorCode();

    public InvalidOtpException(String message) {
        super(message);
    }
}