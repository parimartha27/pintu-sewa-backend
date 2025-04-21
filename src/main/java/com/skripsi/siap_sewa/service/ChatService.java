package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.chat.UnreadCountResponse;
import com.skripsi.siap_sewa.dto.chat.ChatMessageDto;
import com.skripsi.siap_sewa.entity.ChatEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.ChatRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;
    private final CommonUtils commonUtils;

    @Transactional
    public ResponseEntity<ApiResponse> processAndSaveMessage(ChatMessageDto chatMessage) {
        try {
            log.info("Memproses dan menyimpan pesan chat: {}", chatMessage);

            ChatEntity chatEntity = modelMapper.map(chatMessage, ChatEntity.class);

            // Set sender type based on who is sending
            if (chatMessage.getSenderId().equals(chatMessage.getCustomerId())) {
                chatEntity.setSenderType(ChatEntity.SenderType.BUYER);
                log.debug("Mengatur tipe pengirim sebagai BUYER");
            } else {
                chatEntity.setSenderType(ChatEntity.SenderType.SELLER);
                log.debug("Mengatur tipe pengirim sebagai SELLER");
            }

            ChatEntity savedEntity = chatRepository.save(chatEntity);
            log.info("Pesan berhasil disimpan dengan ID: {}", savedEntity.getId());

            ChatMessageDto response = modelMapper.map(savedEntity, ChatMessageDto.class);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Gagal memproses dan menyimpan pesan: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> getChatHistory(String customerId, String shopId) {
        try {
            log.info("Mengambil riwayat chat untuk customerId: {} dan shopId: {}", customerId, shopId);

            List<ChatMessageDto> chatHistory = chatRepository.findByCustomerIdAndShopIdOrderByCreatedDtAsc(customerId, shopId)
                    .stream()
                    .map(entity -> modelMapper.map(entity, ChatMessageDto.class))
                    .collect(Collectors.toList());

            if (chatHistory.isEmpty()) {
                log.warn("Tidak ditemukan riwayat chat untuk customerId: {} dan shopId: {}", customerId, shopId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            log.info("Berhasil mengambil {} pesan dari riwayat chat", chatHistory.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, chatHistory);

        } catch (Exception ex) {
            log.error("Gagal mengambil riwayat chat: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    @Transactional
    public ResponseEntity<ApiResponse> markAsRead(String chatId, String userType) {
        try {
            log.info("Menandai chat dengan ID: {} sebagai dibaca oleh: {}", chatId, userType);

            ChatEntity chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> {
                        log.warn("Chat tidak ditemukan dengan ID: {}", chatId);
                        return new DataNotFoundException("Chat dengan ID: " + chatId + " tidak ditemukan");
                    });

            if ("BUYER".equalsIgnoreCase(userType)) {
                chat.setReadByBuyer(true);
                log.debug("Menandai sebagai dibeli oleh pembeli");
            } else if ("SELLER".equalsIgnoreCase(userType)) {
                chat.setReadBySeller(true);
                log.debug("Menandai sebagai dibeli oleh penjual");
            } else {
                log.warn("Tipe pengguna tidak valid: {}", userType);
                return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
            }

            chatRepository.save(chat);
            log.info("Berhasil menandai chat sebagai dibaca");
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, null);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal menandai chat sebagai dibaca: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> getUnreadCount(String userId, String userType) {
        try {
            log.info("Menghitung pesan belum dibaca untuk userId: {} dan userType: {}", userId, userType);

            int count;
            if ("BUYER".equalsIgnoreCase(userType)) {
                count = chatRepository.countByCustomerIdAndIsReadByBuyerFalse(userId);
                log.debug("Menghitung pesan belum dibeli oleh pembeli");
            } else if ("SELLER".equalsIgnoreCase(userType)) {
                count = chatRepository.countByShopIdAndIsReadBySellerFalse(userId);
                log.debug("Menghitung pesan belum dibeli oleh penjual");
            } else {
                log.warn("Tipe pengguna tidak valid: {}", userType);
                return commonUtils.setResponse(ErrorMessageEnum.BAD_REQUEST, null);
            }

            log.info("Ditemukan {} pesan belum dibaca", count);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, new UnreadCountResponse(count));

        } catch (Exception ex) {
            log.error("Gagal menghitung pesan belum dibaca: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}