package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class EmailSendingException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.EMAIL_SENDING_ERROR.getErrorCode();

    public EmailSendingException(String message) {
        super(message);
    }
}