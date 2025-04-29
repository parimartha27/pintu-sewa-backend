package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.dto.shop.*;
import com.skripsi.siap_sewa.dto.shop.dashboard.TransactionResponse;
import com.skripsi.siap_sewa.dto.shop.dashboard.WalletReportResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import com.skripsi.siap_sewa.helper.ProductHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    
    private final CommonUtils utils;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;
    private final WalletReportRepository walletReportRepository;

    public ResponseEntity<ApiResponse> createShop(CreateShopRequest request) {
        
        Optional<CustomerEntity> customer = customerRepository.findById(request.getCustomerId());
        
        if(customer.isEmpty()){
            return utils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer not exist");
        }

        boolean isShopNameExist = shopRepository.existsByName(request.getName());

        if(isShopNameExist){
            return utils.setResponse(ErrorMessageEnum.FAILED, "Shop name already exist");
        }

        if (!request.isSameAddress()) {
            if (request.getStreet() == null || request.getStreet().isBlank()) {
                throw new IllegalArgumentException("Lokasi toko tidak boleh kosong");
            }
            if (request.getStreet().length() < 5 || request.getStreet().length() > 255) {
                throw new IllegalArgumentException("Lokasi toko harus terdiri dari 5 hingga 255 karakter");
            }

            if (request.getDistrict() == null || request.getDistrict().isBlank()) {
                throw new IllegalArgumentException("Kecamatan tidak boleh kosong");
            }

            if (request.getRegency() == null || request.getRegency().isBlank()) {
                throw new IllegalArgumentException("Kota tidak boleh kosong");
            }

            if (request.getProvince() == null || request.getProvince().isBlank()) {
                throw new IllegalArgumentException("Provinsi tidak boleh kosong");
            }

            if (request.getPostCode() == null || request.getPostCode().isBlank()) {
                throw new IllegalArgumentException("Kode pos tidak boleh kosong");
            }

            if (!request.getPostCode().matches("^[0-9]{5}$")) {
                throw new IllegalArgumentException("Kode pos harus terdiri dari 5 angka");
            }
        }

        CustomerEntity shopOwner = customer.get();
        
        ShopEntity newShop = new ShopEntity();
        
        if(request.isSameAddress()){
            newShop.setStreet(shopOwner.getStreet());
            newShop.setDistrict(shopOwner.getDistrict());
            newShop.setRegency(shopOwner.getRegency());
            newShop.setProvince(shopOwner.getProvince());
            newShop.setPostCode(shopOwner.getPostCode());
        }
        else{
            newShop.setStreet(request.getStreet());
            newShop.setDistrict(request.getDistrict());
            newShop.setRegency(request.getRegency());
            newShop.setProvince(request.getProvince());
            newShop.setPostCode(request.getPostCode());
        }
        
        newShop.setName(request.getName());
        newShop.setEmail(request.getEmail());
        newShop.setShopStatus("ACTIVE");
        newShop.setCustomer(shopOwner);
        newShop.setCreatedAt(LocalDateTime.now());
        newShop.setLastUpdateAt(LocalDateTime.now());
        
        shopRepository.save(newShop);

        CreateShopResponse response = objectMapper.convertValue(newShop, CreateShopResponse.class);
        response.setId(newShop.getId());

        emailService.sendEmail(response.getEmail(), Constant.SUBJECT_EMAIL_REGISTER, commonUtils.generateEmailShop(response.getName()));
        
        return utils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> getShopId(String customerid) {
        try{
            log.info("Finding Shop Id with Customer ID : {}", customerid);
            Optional<CustomerEntity> customer = customerRepository.findById(customerid);
            if(customer.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer Not Found");
            }

            Optional<ShopEntity> shop = shopRepository.findByCustomerId(customerid);
            if(shop.isEmpty()){
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Shop Not Found");
            }else{
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, shop.get().getId());
            }
        } catch (Exception ex) {
            log.error("Error fetching shop ID : {}", ex.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> shopDetail(String shopId) {
        log.info("Start find shop with ID: {}", shopId);
        try {
            ShopEntity shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new DataNotFoundException("Toko tidak ditemukan"));

            ShopDetailResponse response = modelMapper.map(shop, ShopDetailResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop details: {}", ex.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editShop(@Valid EditShopRequest request) {
        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getId());

        if(shopEntity.isPresent()) {
            ShopEntity updatedShop = getShopEntity(request, shopEntity);

            shopRepository.save(updatedShop);

            EditShopResponse response = objectMapper.convertValue(updatedShop, EditShopResponse.class);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        }

        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    private ShopEntity getShopEntity(EditShopRequest request, Optional<ShopEntity> shopEntity) {
        ShopEntity updatedShop = shopEntity.get();

        updatedShop.setName(request.getName());
        updatedShop.setDescription(request.getDescription());
        updatedShop.setImage(request.getImage());
        updatedShop.setStreet(request.getStreet());
        updatedShop.setDistrict(request.getDistrict());
        updatedShop.setRegency(request.getRegency());
        updatedShop.setProvince(request.getProvince());
        updatedShop.setPostCode(request.getPostCode());
        updatedShop.setWorkHours(request.getWorkHours());
        updatedShop.setLastUpdateAt(LocalDateTime.now());
        return updatedShop;
    }

    public ResponseEntity<ApiResponse> getShopDataByProductId(String productId) {
        try {
            log.info("Fetching shop data for product ID: {}", productId);

            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        log.info("Product not found with ID: {}", productId);
                        return new DataNotFoundException("Product not found");
                    });

            ShopEntity shop = product.getShop();
            if (shop == null) {
                log.info("No shop found for product ID: {}", productId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ReviewEntity> allShopReviews = shop.getProducts().stream()
                    .flatMap(p -> p.getReviews().stream())
                    .toList();

            Double shopRating = ProductHelper.calculateWeightedRating(allShopReviews);
            int totalReview = allShopReviews.size();

            ShopByProductResponse response = ShopByProductResponse.builder()
                    .id(shop.getId())
                    .name(shop.getName())
                    .image(shop.getImage())
                    .rating(shopRating)
                    .totalReview(totalReview)
                    .regency(shop.getRegency())
                    .build();

            log.info("Successfully fetched shop data for product ID: {}", productId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop data for product ID {}: {}", productId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getShopDashboardDetail(String shopId) {
        try {
            log.info("Fetching Dashboard Data From Shop : {}", shopId);

            Optional<ShopEntity> shop = shopRepository.findById(shopId);
            if (!shop.isPresent()) {
                log.info("No shop found for this Shop ID : {}", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            ShopEntity shopEntity = shop.get();

            List<ReviewEntity> reviewsPage = reviewRepository.findByProduct_Shop_Id(shopId);
            double reviewsAverage = reviewsPage == null || reviewsPage.isEmpty()
                    ? 0.0
                    : reviewsPage.stream()
                    .mapToDouble(ReviewEntity::getRating)
                    .average()
                    .orElse(0.0);

            int TrasactionCount = transactionRepository.findByShopId(shopId).size();

            List<TransactionEntity> allTransaction = transactionRepository.findByShopIdOrderByCreatedAtDesc(shopId);
            List<TransactionResponse> sortedTransactions = allTransaction.stream()
                    .map(transaction -> TransactionResponse.builder()
                            .refferenceNo(transaction.getTransactionNumber())
                            .createAt(transaction.getCreatedAt().toString()) // atau format sesuai kebutuhan
                            .customerName(transaction.getCustomer().getName()) // asumsi ada relasi ke customer
                            .startDate(transaction.getStartDate().toString())
                            .endDate(transaction.getEndDate().toString())
                            .duration(BigDecimal.valueOf(ChronoUnit.DAYS.between(transaction.getStartDate(), transaction.getEndDate())))
                            .status(transaction.getStatus())
                            .depositStatus(transaction.isDepositReturned())
                            .build())
                    .sorted(Comparator.comparing(TransactionResponse::getCreateAt).reversed()) // contoh sort
                    .limit(5)
                    .toList();

            List<WalletReportEntity> allWalletReport = walletReportRepository.findShopByIdOrderByCreatedAtDesc(shopId);
            List<WalletReportResponse> sortedWallet = allWalletReport.stream()
                    .map(wallet -> WalletReportResponse.builder()
                            .amount(wallet.getAmount())
                            .createAt(wallet.getCreateAt().toString())
                            .description(wallet.getDescription())
                            .type(String.valueOf(wallet.getType()))
                            .build())
                    .sorted(Comparator.comparing(WalletReportResponse::getCreateAt).reversed()) // contoh sort
                    .limit(5)
                    .toList();


            DashboardResponse response = DashboardResponse.builder()
                    .wallet(shopEntity.getBalance())
                    .averageRating(reviewsAverage)
                    .shopStatus(shopEntity.getShopStatus())
                    .TransactionCount(TrasactionCount)
                    .walletReport(sortedWallet)
                    .TransactionList(sortedTransactions).build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop data ID {}: {}", shopId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }


}
