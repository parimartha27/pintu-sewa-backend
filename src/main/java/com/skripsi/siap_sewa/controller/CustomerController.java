package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.customer.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.CloudinaryService;
import com.skripsi.siap_sewa.service.CustomerService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CloudinaryService cloudinaryService;
    private final CommonUtils commonUtils;

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

    @PutMapping(value = "/edit-biodata", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> editBiodata(
            @RequestPart("data") @Valid EditBiodataRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                request.setImage(imageUrl);
            }
            return customerService.editBiodata(request);
        } catch (IOException e) {
            log.error("Gagal upload gambar: {}", e.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
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
