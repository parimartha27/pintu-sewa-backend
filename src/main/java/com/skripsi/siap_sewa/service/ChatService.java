package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.admin.CustomerListResponse;
import com.skripsi.siap_sewa.dto.admin.DashboardResponse;
import com.skripsi.siap_sewa.dto.admin.ShopListResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.entity.chat.ChatDetailEntity;
import com.skripsi.siap_sewa.entity.chat.ChatHeaderEntity;
import com.skripsi.siap_sewa.exception.PhoneNumberExistException;
import com.skripsi.siap_sewa.repository.ChatDetailRepository;
import com.skripsi.siap_sewa.repository.ChatHeaderRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final CommonUtils commonUtils;
    private final AuthenticationManager authManager;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final JWTService jwtService;
    private final ChatHeaderRepository chatHeaderRepository;
    private final ChatDetailRepository chatDetailRepository;

    public ResponseEntity<ApiResponse> createRoomChat(String customerId,String shopId, boolean is_report) {
        try {
            log.info("Create Room Chat");
            Optional<CustomerEntity> customer = customerRepository.findById(customerId);
            Optional<ShopEntity> shop = shopRepository.findById(shopId);

            if(customer.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer is not exist");
            }else if(shop.isEmpty() && !is_report) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Shop is not exist");
            }

            Optional<ChatHeaderEntity> chat = chatHeaderRepository.findByCustomerIdAndShopId(customerId,shopId);

            if(!chat.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.CHAT_FOUND, "Chat Already Exist");
            }

            ChatHeaderEntity chatHeaderEntity = new ChatHeaderEntity();
            chatHeaderEntity.setCustomerId(customerId);
            chatHeaderEntity.setShopId(shopId);
            chatHeaderEntity.setIsReport(is_report);
            chatHeaderRepository.save(chatHeaderEntity);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, chatHeaderEntity);
        }catch (Exception ex) {
            log.info("Gagal Create Room Chat : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> sendMessage(String message,String roomChatId,String senderType) {
        try {
            log.info("Send Message To Room Chat {}", roomChatId);

            Optional<ChatHeaderEntity> roomchat = chatHeaderRepository.findById(roomChatId);

            if(roomchat.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Room Chat is not exist");
            }

            if(!senderType.equals("customer") || !senderType.equals("admin") || !senderType.equals("shop")){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Sender Type Wrong");
            }

            ChatDetailEntity chatDetailEntity = new ChatDetailEntity();
            chatDetailEntity.setChatHeader(roomchat.get());
            chatDetailEntity.setMessage(message);
            chatDetailEntity.setCreatedAt(LocalDateTime.now());
            chatDetailEntity.setSenderType(senderType);
            chatDetailRepository.save(chatDetailEntity);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, chatDetailEntity);
        }catch (Exception ex) {
            log.info("Failed Send Message : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getRoomChatMessage(String roomChatId) {
        try {
            log.info("Get all Message from Room Chat {}", roomChatId);

            List<ChatDetailEntity> list_chat = chatDetailRepository.findByChatHeaderIdOrderByCreatedAtDesc(roomChatId);

            if(list_chat.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "This Room Chat Has no Message");
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, list_chat);
        }catch (Exception ex) {
            log.info("Failed Get Message : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}
