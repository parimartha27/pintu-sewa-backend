package com.skripsi.siap_sewa.dto.chat;

import com.skripsi.siap_sewa.entity.ChatEntity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;
    private String customerId;
    private String shopId;
    private ChatEntity.SenderType senderType;
    private String message;
    private boolean isReadByBuyer;
    private boolean isReadBySeller;
    private LocalDateTime createdDt;

    // Untuk WebSocket
    private String senderId;
    private String recipientId;
}