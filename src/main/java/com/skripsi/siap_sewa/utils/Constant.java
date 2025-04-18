package com.skripsi.siap_sewa.utils;

import org.springframework.http.HttpStatus;

public class Constant {

//    Email Subject
    public static final String SUBJECT_EMAIL_REGISTER = "One-Time Passcode (OTP) kamu dari Pintu Sewa";
    public static final String SUBJECT_EMAIL_CREATE_SHOP = "Selamat Atas Toko Baru Kamu";

//    OTP count
    public static final int MAX_OTP_VERIFY_ATTEMPTS = 10;
    public static final int MAX_OTP_RESEND_ATTEMPTS = 3;
    public static final int TOKEN_EXPIRATION_SECONDS = 1800;

//    Http Status
    public static final HttpStatus  STATUS_OK = HttpStatus.OK;
    public static final HttpStatus  STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;
    public static final HttpStatus STATUS_INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR;

//    General Error
    public static final String SUCCESS_CODE = "PS-00-000";
    public static final String SUCCESS_MESSAGE = "SUCCESS";

    public static final String FAILED_CODE = "PS-99-999";
    public static final String FAILED_MESSAGE = "FAILED";

    public static final String BAD_REQUEST_CODE = "PS-99-400";
    public static final String BAD_REQUEST_MESSAGE = "Request tidak boleh kosong";

    public static final String INTERNAL_SERVER_ERROR_CODE = "PS-99-500";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE ="Internal Server Error";

    public static final String DATA_NOT_FOUND_CODE = "PS-00-002";
    public static final String DATA_NOT_FOUND_MESSAGE = "Data Not Found";

    public static final String USERNAME_EXIST_CODE = "PS-01-001";
    public static final String EMAIL_EXIST_CODE = "PS-01-002";
    public static final String PHONE_NUMBER_EXIST_CODE = "PS-01-003";
    public static final String OTP_ATTEMPTS_EXCEEDED_CODE = "PS-01-005";
    public static final String INVALID_OTP_CODE = "PS-01-006";
    public static final String CART_PRODUCT_NOT_FOUND_CODE = "PS-02-001";
    public static final String INSUFFICIENT_STOCK_CODE = "PS-02-002";
    public static final String CART_ITEM_EXISTS_CODE = "PS-02-003";
    public static final String CHAT_NOT_FOUND_CODE = "PS-03-001";
    public static final String INVALID_USER_TYPE_CODE = "PS-03-002";
    public static final String EMAIL_SENDING_ERROR_CODE = "PS-04-001";
    public static final String INVALID_EMAIL_PARAMETER_CODE = "PS-04-002";
    public static final String PRODUCT_NOT_FOUND_CODE = "PS-05-001";
    public static final String SHOP_NOT_FOUND_CODE = "PS-05-002";
    public static final String PRODUCT_HAS_TRANSACTIONS_CODE = "PS-05-003";
    public static final String REVIEW_NOT_FOUND_CODE = "PS-06-001";
    public static final String CUSTOMER_NOT_FOUND_CODE = "PS-07-001";
    public static final String MAX_QUANTITY_EXCEEDED_CODE = "PS-05-004";
    public static final String MIN_RENT_NOT_MET_CODE = "PS-05-005";
    public static final String UNAUTHORIZED_CART_ACCESS = "PS-02-006";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String TRANSACTION_NUMBER_FORMAT = "yyyyMMddHHmmss";
}
