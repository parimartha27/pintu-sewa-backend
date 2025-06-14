package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.otp.OtpRequest;
import com.skripsi.siap_sewa.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse> resendOtp(@RequestParam String customerId){
        return otpService.resendOtp(customerId);
    }

    @GetMapping("/valid")
    public ResponseEntity<ApiResponse> valid(@RequestParam String customerId){
        return otpService.validateOtp(customerId);
    }
}
