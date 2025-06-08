package com.skripsi.siap_sewa.service;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WhatsappService {

    @Value("${pintu.sewa.whatsapp.url}")
    private String whatsappUrl;
    private final String WAHA_FORMAT = "@c.us";

    public int sendOtpByWhatsapp(String phoneNumber, String otp) {
        String chatId = phoneNumber + WAHA_FORMAT;
        String body = String.format(
                "{\"session\": \"default\", \"chatId\": \"%s\", \"text\": \"Kode OTP kamu adalah: %s\"}",
                chatId, otp
        );

        HttpResponse<kong.unirest.JsonNode> response = Unirest.post(
                whatsappUrl + "/api/sendText")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();
        return response.getStatus();
    }
}
