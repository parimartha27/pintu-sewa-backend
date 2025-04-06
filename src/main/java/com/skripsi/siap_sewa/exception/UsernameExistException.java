package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class UsernameExistException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.USERNAME_EXIST.getErrorCode();

    public UsernameExistException(String message) {
        super(message);
    }
}