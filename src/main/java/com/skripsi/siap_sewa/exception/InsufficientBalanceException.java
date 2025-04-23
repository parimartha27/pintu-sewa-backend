package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final String errorCode;
    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super("Insufficient balance. Current: " + currentBalance + ", Required: " + requiredAmount);
        this.errorCode = ErrorMessageEnum.INSUFFICIENT_BALANCE.getErrorCode();
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }
}
