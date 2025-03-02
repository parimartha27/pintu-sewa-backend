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
    BAD_REQUEST(Constant.FAILED_CODE, Constant.FAILED_MESSAGE, Constant.STATUS_OK),
    DATA_NOT_FOUND(Constant.DATA_NOT_FOUND_CODE, Constant.DATA_NOT_FOUND_MESSAGE, Constant.STATUS_OK);

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
}
