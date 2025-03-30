package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.customer.CreateNewCustomerRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.customer.ForgetPasswordRequest;
import com.skripsi.siap_sewa.dto.customer.ValidateCredentialRequest;
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

    @PutMapping("/create")
    public ResponseEntity<ApiResponse> inputNewCustomerData(@RequestBody @Valid CreateNewCustomerRequest request){
        return customerService.inputCustomerData(request);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editCustomerData(@RequestBody @Valid EditCustomerRequest request){
        return customerService.editCustomerData(request);
    }

    @PostMapping("/validate/credential")
    public ResponseEntity<ApiResponse> validateCredential(@RequestBody @Valid ValidateCredentialRequest request){
        return customerService.validateCredential(request);
    }

    @PutMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody @Valid ForgetPasswordRequest request){
        return customerService.forgetPassword(request);
    }

}
