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
    INTERNAL_SERVER_ERROR(Constant.INTERNAL_SERVER_ERROR_CODE,Constant.INTERNAL_SERVER_ERROR_MESSAGE, Constant.STATUS_INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
}
