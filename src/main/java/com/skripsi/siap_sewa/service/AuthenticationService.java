package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.OtpRequest;
import com.skripsi.siap_sewa.dto.OtpResponse;
import com.skripsi.siap_sewa.dto.SignUpRequest;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CommonUtils commonUtils;

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,}$";
    private static final String PHONE_REGEX = "^(08|\\+628)\\d{7,10}$";
    private static final int OTP_LENGTH = 6;

    public ResponseEntity<ApiResponse> validateOtp(OtpRequest request){
//        TODO: enhance this
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, request);
    }

    public ResponseEntity<ApiResponse> signUp(SignUpRequest request) {

        if (ObjectUtils.isEmpty(request)) {
            return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
        }

        boolean isValid = validateRequest(request);
        if (!isValid) {
            return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
        }

        String otpCode = generateOTP();
        OtpResponse response = new OtpResponse(otpCode, 1);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    private boolean validateRequest(SignUpRequest request) {
        if (StringUtils.hasText(request.getEmail())) {
            return validateEmail(request.getEmail());
        }
        return StringUtils.hasText(request.getPhoneNumber()) && validatePhoneNumber(request.getPhoneNumber());
    }

    private boolean validateEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber.matches(PHONE_REGEX);
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

}
