package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class CheckoutValidationException extends RuntimeException {
    private final String errorCode;

    public CheckoutValidationException(ErrorMessageEnum errorMessage, String detail) {
        super(errorMessage.getErrorMessage() + ": " + detail);
        this.errorCode = errorMessage.getErrorCode();
    }

    public CheckoutValidationException(ErrorMessageEnum errorMessage) {
        super(errorMessage.getErrorMessage());
        this.errorCode = errorMessage.getErrorCode();
    }
}