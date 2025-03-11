package com.skripsi.siap_sewa.exception;

public class EmailExistException extends RuntimeException{

    public EmailExistException() {}

    public EmailExistException(String message) {
        super(message);
    }
}
