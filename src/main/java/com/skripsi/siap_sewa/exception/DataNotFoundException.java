package com.skripsi.siap_sewa.exception;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException() {}

    public DataNotFoundException(String message) {
        super(message);
    }
}
