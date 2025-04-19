package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor

public class AdminController {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginAdmin(@RequestBody @Valid AdminLoginRequest request) {
        log.info("Login Admin attempt for username: {}", request.getUsername());
        return adminService.loginAdmin(request);
    }

    @PostMapping("/dashboard")
    public ResponseEntity<ApiResponse> viewDashboard() {
        log.info("Get All Counts Data");
        return adminService.viewDashboard();
    }

    @GetMapping("/manage-customer/{page}")
    public ResponseEntity<ApiResponse> getAllCustomers(@PathVariable int page) {
        log.info("Get All Customer Data Page : {}", page);
        return adminService.getAllCustomers(page);
    }

    @GetMapping("/manage-shop/{page}")
    public ResponseEntity<ApiResponse> getAllShops(@PathVariable int page) {
        log.info("Get All Shops Data Page : {}", page);
        return adminService.getAllShops(page);
    }

    @PatchMapping("/logout/{id}")
    public ResponseEntity<ApiResponse> adminLogout(@PathVariable String id) {
        log.info("Update Admin Logout : {} ", id);
        return adminService.adminLogout(id);
    }
}
