package com.skripsi.siap_sewa.exception;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import lombok.Getter;

@Getter
public class CheckoutProcessingException extends RuntimeException {
    private final String errorCode;
    private final String productId;

    public CheckoutProcessingException(ErrorMessageEnum errorMessage, String productId, String detail) {
        super(String.format("Gagal memproses checkout untuk produk %s: %s", productId, detail));
        this.errorCode = errorMessage.getErrorCode();
        this.productId = productId;
    }

}