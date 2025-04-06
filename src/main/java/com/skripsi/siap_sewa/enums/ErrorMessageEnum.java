package com.skripsi.siap_sewa.enums;

import com.skripsi.siap_sewa.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorMessageEnum {
    SUCCESS(Constant.SUCCESS_CODE, Constant.SUCCESS_MESSAGE, Constant.STATUS_OK),
    FAILED(Constant.FAILED_CODE, Constant.FAILED_MESSAGE, Constant.STATUS_OK),
    BAD_REQUEST(Constant.BAD_REQUEST_CODE, Constant.BAD_REQUEST_MESSAGE, Constant.STATUS_BAD_REQUEST),
    DATA_NOT_FOUND(Constant.DATA_NOT_FOUND_CODE, Constant.DATA_NOT_FOUND_MESSAGE, Constant.STATUS_OK),
    INTERNAL_SERVER_ERROR(Constant.INTERNAL_SERVER_ERROR_CODE,Constant.INTERNAL_SERVER_ERROR_MESSAGE, Constant.STATUS_INTERNAL_SERVER_ERROR),

    CART_PRODUCT_NOT_FOUND(Constant.CART_PRODUCT_NOT_FOUND_CODE, "Produk tidak ditemukan di keranjang", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(Constant.INSUFFICIENT_STOCK_CODE, "Stok produk tidak mencukupi", HttpStatus.BAD_REQUEST),
    CART_ITEM_EXISTS(Constant.CART_ITEM_EXISTS_CODE, "Produk sudah ada di keranjang", HttpStatus.CONFLICT),

    CHAT_NOT_FOUND(Constant.CHAT_NOT_FOUND_CODE, "Chat tidak ditemukan", HttpStatus.NOT_FOUND),
    INVALID_USER_TYPE(Constant.INVALID_USER_TYPE_CODE, "Tipe pengguna tidak valid", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
}
