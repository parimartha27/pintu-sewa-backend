package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class PhoneNumberExistException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.PHONE_NUMBER_EXIST.getErrorCode();

    public PhoneNumberExistException(String message) {
        super(message);
    }
}