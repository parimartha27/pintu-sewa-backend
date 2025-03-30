package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.otp.OtpRequest;
import com.skripsi.siap_sewa.dto.authentication.otp.ResendOtpRequest;
import com.skripsi.siap_sewa.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody @Valid OtpRequest request){
        return otpService.verifyOtp(request);
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse> resendOtp(@RequestBody @Valid ResendOtpRequest request){
        return otpService.resendOtp(request);
    }
}
