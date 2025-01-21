package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.OtpRequest;
import com.skripsi.siap_sewa.dto.SignUpRequest;
import com.skripsi.siap_sewa.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest request){
        return authenticationService.signUp(request);
    }

    @PostMapping("/validate/otp")
    public ResponseEntity<ApiResponse> validateOtp(@RequestBody OtpRequest request){
        return authenticationService.validateOtp(request);
    }
}
