package com.skripsi.siap_sewa.exception;

public class PhoneNumberExistException extends RuntimeException {
    private final String errorCode = "SIAP-SEWA-01-004";

    public PhoneNumberExistException() {
        super("Phone number has been registered. Please use other phone number");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
