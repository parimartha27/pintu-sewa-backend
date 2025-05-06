package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.customer.*;
import com.skripsi.siap_sewa.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
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

    @PutMapping("/edit-address")
    public ResponseEntity<ApiResponse> editAddress(@RequestBody @Valid EditAddressRequest request) {
        return customerService.editAddress(request);
    }

    @PutMapping(value = "/edit-biodata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> editBiodata(
            @Valid EditBiodataRequest request) {
        return customerService.editBiodata(request);
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


    @PostMapping(value = "/create/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> inputNewCustomerWithImage(
            @Valid CreateNewCustomerRequest request) {
        return customerService.inputCustomerDataWithImage(request);
    }
}
