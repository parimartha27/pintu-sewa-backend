package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.customer.*;
import com.skripsi.siap_sewa.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCustomerDetails(@PathVariable String id) {
        return customerService.getCustomerDetails(id);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> inputNewCustomerData(@RequestBody @Valid CreateNewCustomerRequest request){
        return customerService.inputCustomerData(request);
    }

    @PutMapping("/edit-biodata")
    public ResponseEntity<ApiResponse> editBiodata(@RequestBody @Valid EditBiodataRequest request) {
        return customerService.editBiodata(request);
    }

    @PutMapping("/edit-address")
    public ResponseEntity<ApiResponse> editAddress(@RequestBody @Valid EditAddressRequest request) {
        return customerService.editAddress(request);
    }

    @PostMapping("/validate/credential")
    public ResponseEntity<ApiResponse> validateCredential(@RequestBody @Valid ValidateCredentialRequest request){
        return customerService.validateCredential(request);
    }

    @PutMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody @Valid ForgetPasswordRequest request){
        return customerService.forgetPassword(request);
    }

    @GetMapping("/address/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerAddress(@PathVariable String customerId){
        return customerService.getCustomerAddress(customerId);
    }

}
