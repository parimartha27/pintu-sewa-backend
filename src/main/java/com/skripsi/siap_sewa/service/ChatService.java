package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.chat.UnreadCountResponse;
import com.skripsi.siap_sewa.dto.chat.ChatMessageDto;
import com.skripsi.siap_sewa.entity.ChatEntity;
import com.skripsi.siap_sewa.exception.ChatException;
import com.skripsi.siap_sewa.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ChatMessageDto processAndSaveMessage(ChatMessageDto chatMessage) {
        ChatEntity chatEntity = modelMapper.map(chatMessage, ChatEntity.class);

        // Set sender type based on who is sending
        if (chatMessage.getSenderId().equals(chatMessage.getCustomerId())) {
            chatEntity.setSenderType(ChatEntity.SenderType.BUYER);
        } else {
            chatEntity.setSenderType(ChatEntity.SenderType.SELLER);
        }

        ChatEntity savedEntity = chatRepository.save(chatEntity);
        return modelMapper.map(savedEntity, ChatMessageDto.class);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(String customerId, String shopId) {
        return chatRepository.findByCustomerIdAndShopIdOrderByCreatedDtAsc(customerId, shopId)
                .stream()
                .map(entity -> modelMapper.map(entity, ChatMessageDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String chatId, String userType) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatException("Chat not found"));

        if ("BUYER".equalsIgnoreCase(userType)) {
            chat.setReadByBuyer(true);
        } else if ("SELLER".equalsIgnoreCase(userType)) {
            chat.setReadBySeller(true);
        }

        chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userId, String userType) {
        int count;
        if ("BUYER".equalsIgnoreCase(userType)) {
            count = chatRepository.countByCustomerIdAndIsReadByBuyerFalse(userId);
        } else {
            count = chatRepository.countByShopIdAndIsReadBySellerFalse(userId);
        }
        return new UnreadCountResponse(count);
    }
}