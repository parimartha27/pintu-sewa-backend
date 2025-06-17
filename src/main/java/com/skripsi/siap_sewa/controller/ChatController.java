package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.chat.SendMessageRequest;
import com.skripsi.siap_sewa.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor

public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create-roomchat")
    public ResponseEntity<ApiResponse> createRoomChat(
            @RequestParam String customerId,
            @RequestParam String shopId,
            @RequestParam(required = false,defaultValue = "false") boolean is_report) {
        log.info("Create New Room Chat For Customer ID {} With Shop ID : {}", customerId, shopId);
        return chatService.createRoomChat(customerId,shopId,is_report);
    }

    @GetMapping("/customer/get-roomchat")
    public ResponseEntity<ApiResponse> customerGetRoomChat(@RequestParam String id){
        log.info("Get List Room Chat For Customer with id {}",id);
        return chatService.customerGetRoomChat(id);
    }

    @GetMapping("/shop/get-roomchat")
    public ResponseEntity<ApiResponse> shopGetRoomChat(@RequestParam String id){
        log.info("Get List Room Chat For Shop with id {}",id);
        return chatService.shopGetRoomChat(id);
    }

    @DeleteMapping("/delete-roomchat")
    public ResponseEntity<ApiResponse> deleteRoomChat(@RequestParam String id){
        log.info("Delete Room Chat with id {}",id);
        return chatService.deleteRoomChat(id);
    }

    @PostMapping("/send-message")
    public ResponseEntity<ApiResponse> sendMessage(@RequestBody @Valid SendMessageRequest request) {
        log.info("Send Message to RoomChatID {} from {}", request.getRoomChatId(), request.getSenderType());
        return chatService.sendMessage(request);
    }

    @GetMapping("/view-roomchat/{roomChatId}")
    public ResponseEntity<ApiResponse> getRoomChatMessage(
            @PathVariable String roomChatId) {
        log.info("Get All Message From Roomchat : {} ", roomChatId);
        return chatService.getRoomChatMessage(roomChatId);
    }

    @GetMapping("/admin/get-roomchat")
    public ResponseEntity<ApiResponse> AdminGetRoomChat(@RequestParam String id){
        log.info("Get List Room Chat For Admin");
        return chatService.adminGetRoomChat(id);
    }
}
