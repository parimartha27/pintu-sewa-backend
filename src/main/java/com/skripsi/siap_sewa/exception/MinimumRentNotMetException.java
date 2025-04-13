package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class MinimumRentNotMetException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.MIN_RENT_NOT_MET.getErrorCode();
    private final int minRent;
    private final int requestedQuantity;

    public MinimumRentNotMetException(int minRent, int requestedQuantity) {
        super(String.format("Jumlah minimum sewa tidak terpenuhi. Minimum: %d, Diminta: %d",
                minRent, requestedQuantity));
        this.minRent = minRent;
        this.requestedQuantity = requestedQuantity;
    }
}