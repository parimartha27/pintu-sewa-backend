package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminRequest;
import com.skripsi.siap_sewa.dto.authentication.login.LoginRequest;
import com.skripsi.siap_sewa.service.AdminService;
import com.skripsi.siap_sewa.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.beans.Encoder;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor

public class AdminController {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginAdmin(@RequestBody @Valid AdminRequest request) {
        log.info("Login Admin attempt for username: {}", request.getUsername());
        return adminService.loginAdmin(request);
    }
}
