package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.chat.ChatMessageDto;
import com.skripsi.siap_sewa.dto.chat.UnreadCountResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.service.ChatService;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final CommonUtils commonUtils;

    @MessageMapping("/send")
    public void processMessage(@Payload ChatMessageDto chatMessage) {
        try {
            ResponseEntity<ApiResponse> savedMsg = chatService.processAndSaveMessage(chatMessage);

            // Notify buyer
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getCustomerId(), "/queue/messages",
                    savedMsg
            );

            // Notify seller
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getShopId(), "/queue/messages",
                    savedMsg
            );
        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId(), "/queue/errors",
                    "Failed to send message: " + e.getMessage()
            );
        }
    }

    @GetMapping("/history/{customerId}/{shopId}")
    public ResponseEntity<ApiResponse> getChatHistory(
            @PathVariable String customerId,
            @PathVariable String shopId) {
        try {
            List<ChatMessageDto> messages = (List<ChatMessageDto>) chatService.getChatHistory(customerId, shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, messages);
        } catch (Exception e) {
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    @PostMapping("/mark-as-read/{chatId}/{userType}")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable String chatId,
            @PathVariable String userType) {
        try {
            chatService.markAsRead(chatId, userType);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, null);
        } catch (Exception e) {
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/unread-count/{userId}/{userType}")
    public ResponseEntity<ApiResponse> getUnreadCount(
            @PathVariable String userId,
            @PathVariable String userType) {
        try {
            ResponseEntity<ApiResponse> count = chatService.getUnreadCount(userId, userType);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, count);
        } catch (Exception e) {
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}