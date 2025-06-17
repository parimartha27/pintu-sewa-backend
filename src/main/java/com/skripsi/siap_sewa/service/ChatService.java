package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.admin.CustomerListResponse;
import com.skripsi.siap_sewa.dto.admin.DashboardResponse;
import com.skripsi.siap_sewa.dto.admin.ShopListResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.chat.GroupedMessageResponse;
import com.skripsi.siap_sewa.dto.chat.ListChatResponse;
import com.skripsi.siap_sewa.dto.chat.MessageResponse;
import com.skripsi.siap_sewa.dto.chat.SendMessageRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.dashboard.WalletReportResponse;
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
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

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

    public ResponseEntity<ApiResponse> customerGetRoomChat(String id) {
        try {
            log.info("Get Room Chat For Customer : {}", id);

            Optional<CustomerEntity> customer = customerRepository.findById(id);
            if (customer.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer Not Found");
            }

            List<ChatHeaderEntity> listChat = chatHeaderRepository.findByIsReportAndCustomerId(false, id);

            if (listChat.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "There is no chat available");
            }

            List<ListChatResponse> newListChat = listChat.stream()
                    .map(chat -> ListChatResponse.builder()
                            .Image(shopRepository.findById(chat.getShopId()).get().getImage())
                            .name(shopRepository.findById(chat.getShopId()).get().getName())
                            .shopId(chat.getShopId())
                            .customerId(chat.getCustomerId())
                            .id(chat.getId())
                            .build())
                    .toList();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, newListChat);
        }catch (Exception ex) {
            log.info("Failed Get RoomChat List : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> ReportGetRoomchat(String id) {
        try {
            log.info("Get Room Chat For Customer : {}", id);

            Optional<CustomerEntity> customer = customerRepository.findById(id);
            if (customer.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer Not Found");
            }

            Optional<ChatHeaderEntity> listChat = chatHeaderRepository.findByCustomerIdAndIsReport(id,true);
            if(listChat.isEmpty()) {
                ChatHeaderEntity newRoomChat = new ChatHeaderEntity();
                newRoomChat.setCustomerId(id);
                newRoomChat.setIsReport(true);
                newRoomChat.setShopId("admin");
                ChatHeaderEntity savedChat = chatHeaderRepository.save(newRoomChat);
                String generatedId = savedChat.getId();

                Optional<ChatHeaderEntity> roomchat = chatHeaderRepository.findById(generatedId);

                ChatDetailEntity chatDetailEntity = new ChatDetailEntity();
                chatDetailEntity.setChatHeader(roomchat.get());
                chatDetailEntity.setMessage("Hallo , Terima Kasih telah menghubungi Admin Pintu Sewa, Ada yang bisa kami bantu?");
                chatDetailEntity.setCreatedAt(LocalDateTime.now());
                chatDetailEntity.setSenderType("shop");
                chatDetailRepository.save(chatDetailEntity);

                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, generatedId);
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, listChat.get().getId());
        }catch (Exception ex) {
            log.info("Failed Get RoomChat List : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> deleteRoomChat(String id) {
        try {
            log.info("Delete Room Chat with Id : {}", id);

            Optional<ChatHeaderEntity> chat = chatHeaderRepository.findById(id);
            if (chat.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Chat Not Found");
            }

            List<ChatDetailEntity> listchat = chatDetailRepository.findByChatHeaderIdOrderByCreatedAtAsc(id);
            if(!listchat.isEmpty()){
                chatDetailRepository.deleteAll(listchat);
            }

            chatHeaderRepository.deleteById(id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Chat deleted successfully");
        }catch (Exception ex) {
            log.info("Failed Get RoomChat List : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> shopGetRoomChat(String shopId) {
        try {
            log.info("Get Room Chat For Shop : {}", shopId);

            Optional<ShopEntity> shop = shopRepository.findById(shopId);
            if (shop.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Shop Not Found");
            }

            List<ChatHeaderEntity> listChat = chatHeaderRepository.findByShopId(shopId);
            if (listChat.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "There is no Chat Available");
            }

            List<ListChatResponse> newListChat = listChat.stream()
                    .map(chat -> ListChatResponse.builder()
                            .Image(customerRepository.findById(chat.getCustomerId()).get().getImage())
                            .name(customerRepository.findById(chat.getCustomerId()).get().getName())
                            .id(chat.getId())
                            .build())
                    .toList();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, newListChat);
        }catch (Exception ex) {
            log.info("Failed Get RoomChat List : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> adminGetRoomChat() {
        try {
            log.info("Get Room Chat For Admin");

            List<ChatHeaderEntity> listChat = chatHeaderRepository.findByIsReport(Boolean.TRUE);
            if (listChat.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "There is no Chat Available");
            }

            List<ListChatResponse> newListChat = listChat.stream()
                    .map(chat -> ListChatResponse.builder()
                            .Image(customerRepository.findById(chat.getCustomerId()).get().getImage())
                            .name(customerRepository.findById(chat.getCustomerId()).get().getName())
                            .id(chat.getId())
                            .build())
                    .toList();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, newListChat);
        }catch (Exception ex) {
            log.info("Failed Get RoomChat List : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> sendMessage(@Valid SendMessageRequest request) {
        try {
            log.info("Send Message To Room Chat {}", request.getRoomChatId());

            Optional<ChatHeaderEntity> roomchat = chatHeaderRepository.findById(request.getRoomChatId());

            if(roomchat.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Room Chat is not exist");
            }

            if (
                    !request.getSenderType().equals("customer") &&
                            !request.getSenderType().equals("admin") &&
                            !request.getSenderType().equals("shop")
            ) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Sender Type Wrong");
            }


            ChatDetailEntity chatDetailEntity = new ChatDetailEntity();
            chatDetailEntity.setChatHeader(roomchat.get());
            chatDetailEntity.setMessage(request.getMessage());
            chatDetailEntity.setCreatedAt(LocalDateTime.now());
            chatDetailEntity.setSenderType(request.getSenderType());
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

            List<ChatDetailEntity> listChat = chatDetailRepository.findByChatHeaderIdOrderByCreatedAtAsc(roomChatId);

            if (listChat.isEmpty()) {
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "This Room Chat Has no Message");
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")
            );
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


            List<MessageResponse> allMessages = listChat.stream()
                    .map(chat -> MessageResponse.builder()
                            .message(chat.getMessage())
                            .time(chat.getCreatedAt().format(timeFormatter))
                            .senderType(chat.getSenderType())
                            .date(chat.getCreatedAt().format(dateFormatter))
                            .build())
                    .collect(Collectors.toList());

            List<GroupedMessageResponse> groupedMessages = allMessages.stream()
                    .collect(Collectors.groupingBy(MessageResponse::getDate))
                    .entrySet()
                    .stream()
                    .map(entry -> new GroupedMessageResponse(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparing(GroupedMessageResponse::getDate))
                    .collect(Collectors.toList());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, groupedMessages);
        } catch (Exception ex) {
            log.info("Failed Get Message : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

}
