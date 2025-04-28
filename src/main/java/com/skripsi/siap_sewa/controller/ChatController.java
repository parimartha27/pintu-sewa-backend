package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.service.AdminService;
import com.skripsi.siap_sewa.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor

public class ChatController {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final ChatService chatService;

    @PostMapping("/create-roomchat")
    public ResponseEntity<ApiResponse> createRoomChat(
            @RequestParam String customerId,
            @RequestParam String shopId,
            @RequestParam(required = false,defaultValue = "false") boolean is_report) {
        log.info("Create New Room Chat For Customer ID {} With Shop ID : {}", customerId, shopId);
        return chatService.createRoomChat(customerId,shopId,is_report);
    }

    @PostMapping("/send-message")
    public ResponseEntity<ApiResponse> sendMessage(
            @RequestParam String message,
            @RequestParam String roomChatId,
            @RequestParam String senderType) {
        log.info("Send Message to RoomChatID {} from {}", roomChatId, senderType);
        return chatService.sendMessage(message,roomChatId,senderType);
    }

    @GetMapping("/view-roomchat/{roomChatId}")
    public ResponseEntity<ApiResponse> createRoomChat(
            @PathVariable String roomChatId) {
        log.info("Get All Message From Roomchat : {} ", roomChatId);
        return chatService.getRoomChatMessage(roomChatId);
    }
}
