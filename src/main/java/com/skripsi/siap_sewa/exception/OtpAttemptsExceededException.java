package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class OtpAttemptsExceededException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.OTP_ATTEMPTS_EXCEEDED.getErrorCode();

    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}