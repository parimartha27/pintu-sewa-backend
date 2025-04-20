package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ekspedisi")
@RequiredArgsConstructor
public class EkspedisiController {

    private final CommonUtils commonUtils;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllShippingPartners() {
        try {
            log.info("Getting all shipping partners");
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, Constant.SHIPPING_PARTNERS);
        } catch (Exception e) {
            log.error("Error getting shipping partners: {}", e.getMessage(), e);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}