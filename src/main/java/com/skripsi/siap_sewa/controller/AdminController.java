package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
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
@CrossOrigin(origins = {"https://pintu-sewa-admin.up.railway.app", "https://pintu-sewa.up.railway.app"})
public class AdminController {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginAdmin(@RequestBody @Valid AdminLoginRequest request) {
        log.info("Login Admin attempt for username: {}", request.getUsername());
        return adminService.loginAdmin(request);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> viewDashboard() {
        log.info("Get All Counts Data");
        return adminService.viewDashboard();
    }

    @GetMapping("/manage-customer/{page}")
    public ResponseEntity<ApiResponse> getAllCustomers(@PathVariable int page) {
        log.info("Get All Customer Data Page : {}", page);
        return adminService.getAllCustomers(page);
    }

    @PutMapping("/manage-customer/edit-biodata")
    public ResponseEntity<ApiResponse> editBiodata(@RequestBody @Valid EditCustomerRequest request) {
        return adminService.editBiodata(request);
    }

    @PutMapping("/manage-customer/suspend/{id}")
    public ResponseEntity<ApiResponse> suspendCustomer(@PathVariable String id){
        return adminService.suspendCustomer(id);
    }

    @PutMapping("/manage-customer/unsuspend/{id}")
    public ResponseEntity<ApiResponse> unSuspendCustomer(@PathVariable String id){
        return adminService.unSuspendCustomer(id);
    }

    @GetMapping("/manage-shop/{page}")
    public ResponseEntity<ApiResponse> getAllShops(@PathVariable int page) {
        log.info("Get All Shops Data Page : {}", page);
        return adminService.getAllShops(page);
    }

    @PutMapping("/manage-shop/edit")
    public ResponseEntity<ApiResponse> editShop(@RequestBody @Valid EditShopRequest request) {
        return adminService.editShop(request);
    }

    @PutMapping("/manage-shop/deactive/{id}")
    public ResponseEntity<ApiResponse> deactiveShop(@PathVariable String id){
        return adminService.deactiveShop(id);
    }

    @PutMapping("/manage-shop/activate/{id}")
    public ResponseEntity<ApiResponse> activeShop(@PathVariable String id){
        return adminService.activeShop(id);
    }

    @PatchMapping("/logout/{id}")
    public ResponseEntity<ApiResponse> adminLogout(@PathVariable String id) {
        log.info("Update Admin Logout : {} ", id);
        return adminService.adminLogout(id);
    }
}
