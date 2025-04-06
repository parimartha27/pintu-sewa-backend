package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.login.LoginRequest;
import com.skripsi.siap_sewa.dto.authentication.register.RegisterOauthRequest;
import com.skripsi.siap_sewa.dto.authentication.register.RegisterRequest;
import com.skripsi.siap_sewa.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Register request received for email: {} or phone: {}",
                request.getEmail(), request.getPhoneNumber());
        return authenticationService.register(request);
    }

    @PostMapping("/register/oauth")
    public ResponseEntity<ApiResponse> registerOauth(@RequestBody @Valid RegisterOauthRequest request) {
        log.info("OAuth register request received for email: {}", request.getEmail());
        return authenticationService.registerOauth(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Login attempt for email/phone: {}",
                request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
        return authenticationService.login(request);
    }
}
