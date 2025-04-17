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
    INVALID_USER_TYPE(Constant.INVALID_USER_TYPE_CODE, "Tipe pengguna tidak valid", HttpStatus.BAD_REQUEST),

    USERNAME_EXIST(Constant.USERNAME_EXIST_CODE, "Username sudah digunakan", HttpStatus.BAD_REQUEST),
    EMAIL_EXIST(Constant.EMAIL_EXIST_CODE, "Email sudah digunakan", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_EXIST(Constant.PHONE_NUMBER_EXIST_CODE, "Nomor telepon sudah digunakan", HttpStatus.BAD_REQUEST),

    EMAIL_SENDING_ERROR(Constant.EMAIL_SENDING_ERROR_CODE, "Gagal mengirim email", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_EMAIL_PARAMETER(Constant.INVALID_EMAIL_PARAMETER_CODE, "Parameter email tidak valid", HttpStatus.BAD_REQUEST),

    OTP_ATTEMPTS_EXCEEDED(Constant.OTP_ATTEMPTS_EXCEEDED_CODE, "Percobaan OTP telah habis", HttpStatus.BAD_REQUEST),
    INVALID_OTP(Constant.INVALID_OTP_CODE, "Kode OTP tidak valid", HttpStatus.BAD_REQUEST),

    PRODUCT_NOT_FOUND(Constant.PRODUCT_NOT_FOUND_CODE, "Produk tidak ditemukan", HttpStatus.NOT_FOUND),
    SHOP_NOT_FOUND(Constant.SHOP_NOT_FOUND_CODE, "Toko tidak ditemukan", HttpStatus.NOT_FOUND),
    PRODUCT_HAS_TRANSACTIONS(Constant.PRODUCT_HAS_TRANSACTIONS_CODE, "Produk tidak dapat dihapus karena memiliki transaksi terkait", HttpStatus.BAD_REQUEST),

    NO_REVIEWS_FOUND(Constant.REVIEW_NOT_FOUND_CODE, "Review tidak ditemukan", HttpStatus.NOT_FOUND),
    CUSTOMER_NOT_FOUND(Constant.CUSTOMER_NOT_FOUND_CODE, "Customer tidak ditemukan", HttpStatus.NOT_FOUND),
    MAX_QUANTITY_EXCEEDED(Constant.MAX_QUANTITY_EXCEEDED_CODE, "Jumlah maximum sewa telah tercapai", HttpStatus.BAD_REQUEST),
    MIN_RENT_NOT_MET(Constant.MIN_RENT_NOT_MET_CODE, "Jumlah minimum sewa belum tercapai", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_CART_ACCESS(Constant.UNAUTHORIZED_CART_ACCESS, "Tidak ada akses ke cart" , HttpStatus.UNAUTHORIZED ),
    CHECKOUT_VALIDATION_FAILED("PS-08-001", "Validasi checkout gagal", HttpStatus.BAD_REQUEST),
    PRODUCT_UNAVAILABLE("PS-08-002", "Produk tidak tersedia", HttpStatus.BAD_REQUEST),
    SHIPPING_CALCULATION_FAILED("PS-08-003", "Gagal menghitung biaya pengiriman", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_EDIT_EXPIRED("PS-07-002", "Email hanya bisa diubah dalam 30 hari pertama", HttpStatus.BAD_REQUEST),
    USERNAME_EDIT_EXPIRED("PS-07-003", "Username hanya bisa diubah dalam 30 hari sejak terakhir update", HttpStatus.BAD_REQUEST),;

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
}
