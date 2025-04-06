package com.skripsi.siap_sewa.utils;

import org.springframework.http.HttpStatus;

public class Constant {

    public static final String SUCCESS_CODE = "PS-00-000";
    public static final String SUCCESS_MESSAGE = "SUCCESS";

    public static final String FAILED_CODE = "PS-99-999";
    public static final String FAILED_MESSAGE = "FAILED";

    public static final String BAD_REQUEST_CODE = "PS-99-400";
    public static final String BAD_REQUEST_MESSAGE = "Request tidak boleh kosong";

    public static final HttpStatus  STATUS_OK = HttpStatus.OK;
    public static final HttpStatus  STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;

    public static final String DATA_NOT_FOUND_CODE = "PS-00-002";
    public static final String DATA_NOT_FOUND_MESSAGE = "Data Not Found";

    public static final String SUBJECT_EMAIL_REGISTER = "One-Time Passcode (OTP) kamu dari Pintu Sewa";
    public static final String SUBJECT_EMAIL_CREATE_SHOP = "Selamat Atas Toko Baru Kamu";

    public static final String INTERNAL_SERVER_ERROR_CODE = "PS-99-500";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE ="Internal Server Error";
    public static final HttpStatus STATUS_INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR;

    public static final String CART_PRODUCT_NOT_FOUND_CODE = "PS-02-001";
    public static final String INSUFFICIENT_STOCK_CODE = "PS-02-002";
    public static final String CART_ITEM_EXISTS_CODE = "PS-02-003";

    public static final String CHAT_NOT_FOUND_CODE = "PS-03-001";
    public static final String INVALID_USER_TYPE_CODE = "PS-03-002";
}
