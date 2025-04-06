package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class EmailExistException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.EMAIL_EXIST.getErrorCode();

    public EmailExistException(String message) {
        super(message);
    }
}