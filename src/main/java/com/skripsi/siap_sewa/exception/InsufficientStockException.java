package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final String errorCode = ErrorMessageEnum.INSUFFICIENT_STOCK.getErrorCode();
    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(int availableStock, int requestedQuantity) {
        super(String.format("Stok tidak mencukupi. Tersedia: %d, Diminta: %d",
                availableStock, requestedQuantity));
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }
}