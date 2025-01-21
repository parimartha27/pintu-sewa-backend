package com.skripsi.siap_sewa.utils;

import org.springframework.http.HttpStatus;

public class Constant {

    public static final String SUCCESS_CODE = "SIAP-SEWA-00-000";
    public static final String SUCCESS_MESSAGE = "SUCCESS";

    public static final String FAILED_CODE = "SIAP-SEWA-99-999";
    public static final String FAILED_MESSAGE = "FAILED";

    public static final String BAD_REQUEST_CODE = "SIAP-SEWA-99-400";
    public static final String BAD_REQUEST_MESSAGE = "Request tidak boleh kosong";

    public static final HttpStatus  STATUS_OK = HttpStatus.OK;
    public static final HttpStatus  STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;
}
