package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.AddCustomerRequest;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.CreateShopRequest;
import com.skripsi.siap_sewa.dto.EditCustomerRequest;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.CustomerService;
import com.skripsi.siap_sewa.service.ShopService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CommonUtils utils;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCustomer(@RequestBody @Valid AddCustomerRequest request){
        return utils.setResponse(ErrorMessageEnum.SUCCESS, customerService.addCustomer(request));
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editCustomerData(@RequestBody @Valid EditCustomerRequest request){
        return utils.setResponse(ErrorMessageEnum.SUCCESS, customerService.editCustomerData(request));
    }
}
