package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.LoginRequest;
import com.skripsi.siap_sewa.dto.authentication.RegisterRequest;
import com.skripsi.siap_sewa.service.AuthenticationService;
import com.skripsi.siap_sewa.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request){
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request){
        return authenticationService.login(request);
    }

    @GetMapping("/email")
    public void email(){
        emailService.sendEmail();
    }

}
