package com.skripsi.siap_sewa.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class TransactionProcessingException extends RuntimeException {
    private final String errorCode;
    private final List<String> failedTransactionIds;

    public TransactionProcessingException(String message, String errorCode, List<String> failedTransactionIds) {
        super(message);
        this.errorCode = errorCode;
        this.failedTransactionIds = failedTransactionIds;
    }
}
