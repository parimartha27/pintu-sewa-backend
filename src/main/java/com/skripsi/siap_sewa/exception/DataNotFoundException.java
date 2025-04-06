package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;

public class DataNotFoundException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.DATA_NOT_FOUND.getErrorCode();

    public DataNotFoundException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
