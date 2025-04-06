package com.skripsi.siap_sewa.exception;

import lombok.Getter;

@Getter
public class EmailExistException extends RuntimeException {
    private final String errorCode = "SIAP-SEWA-01-003";

    public EmailExistException() {
        super("Email has been registered. Please use other email");
    }

}