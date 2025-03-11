package com.skripsi.siap_sewa.exception;

public class PhoneNumberExistException extends RuntimeException {

    public PhoneNumberExistException() {}

    public PhoneNumberExistException(String message) {
        super(message);
    }
}
