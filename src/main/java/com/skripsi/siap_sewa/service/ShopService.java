package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.shop.*;
import com.skripsi.siap_sewa.dto.shop.dashboard.TransactionResponseShopDashboard;
import com.skripsi.siap_sewa.dto.shop.dashboard.WalletReportResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.helper.ProductHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final CloudinaryService cloudinaryService;

    public ResponseEntity<ApiResponse> createShop(CreateShopRequest request) {
        
        Optional<CustomerEntity> customer = customerRepository.findById(request.getCustomerId());
        
        if(customer.isEmpty()){
            return utils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "Customer not exist");
        }

        boolean isShopNameExist = shopRepository.existsByName(request.getName());

        if(isShopNameExist){
            return utils.setResponse(ErrorMessageEnum.FAILED, "Shop name already exist");
        }

        log.info("Shop created {}",request.getIsSameAddress());

        if (request.getIsSameAddress().equals("n")) {
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
        }

        CustomerEntity shopOwner = customer.get();
        
        ShopEntity newShop = new ShopEntity();
        
        if(request.getIsSameAddress().equals("y")){
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

        emailService.sendEmail(response.getEmail(), 1, newShop.getName());
        
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
                CustomerAccessShopResponse response = CustomerAccessShopResponse.builder()
                        .shopId(shop.get().getId())
                        .shopName(shop.get().getName())
                        .shopImage(shop.get().getImage())
                        .build();
                return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,response);
            }
        } catch (Exception ex) {
            log.error("Error fetching shop ID : {}", ex.getMessage());
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> shopDetail(String shopId) {
        log.info("Start find shop with ID: {}", shopId);
        try {
            // Find shop or throw exception if not found
            ShopEntity shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new DataNotFoundException("Toko tidak ditemukan"));

            ShopDetailResponse response = modelMapper.map(shop, ShopDetailResponse.class);

            List<ReviewEntity> reviews = shop.getProducts().stream()
                    .flatMap(product -> product.getReviews().stream())
                    .collect(Collectors.toList());
            
            Double rating = ProductHelper.calculateWeightedRating(reviews);
            response.setRating(rating);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (DataNotFoundException ex) {
            log.error("Shop not found: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching shop details: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editShop(@Valid EditShopRequest request) {
        try {
            log.info("Memproses edit shop dengan ID: {}", request.getId());

            Optional<ShopEntity> shopOpt = shopRepository.findById(request.getId());
            if (shopOpt.isEmpty()) {
                log.info("Shop tidak ditemukan dengan ID: {}", request.getId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            ShopEntity shop = shopOpt.get();

            // Upload image jika berbeda
            String imageUrl = shop.getImage(); // default: existing image
            if (request.getImage() != null && !request.getImage().equals(shop.getImage())) {
                try {
                    imageUrl = cloudinaryService.uploadImage(request.getImage());

//                    // Hapus gambar lama jika dari Cloudinary
//                    if (shop.getImage() != null && shop.getImage().contains("res.cloudinary.com")) {
//                        try {
//                            String publicId = shop.getImage().substring(
//                                    shop.getImage().lastIndexOf("/") + 1,
//                                    shop.getImage().lastIndexOf(".")
//                            );
//                            cloudinaryService.deleteImage(publicId);
//                        } catch (Exception e) {
//                            log.warn("Gagal menghapus gambar lama: {}", e.getMessage());
//                        }
//                    }
                } catch (IllegalArgumentException e) {
                    return commonUtils.setResponse(
                            ErrorMessageEnum.INVALID_FILE_FORMAT,
                            Map.of("message", e.getMessage())
                    );
                } catch (IOException e) {
                    log.error("Gagal upload gambar: {}", e.getMessage());
                    return commonUtils.setResponse(ErrorMessageEnum.IMAGE_UPLOAD_FAILED, null);
                }
            }

            // Set field baru
            shop.setName(request.getName());
            shop.setDescription(request.getDescription());
            shop.setImage(imageUrl);
            shop.setStreet(request.getStreet());
            shop.setDistrict(request.getDistrict());
            shop.setRegency(request.getRegency());
            shop.setProvince(request.getProvince());
            shop.setPostCode(request.getPostCode());
            shop.setWorkHours(request.getWorkHours());
            shop.setLastUpdateAt(LocalDateTime.now());

            shopRepository.save(shop);

            EditShopResponse response = EditShopResponse.builder()
                    .name(shop.getName())
                    .description(shop.getDescription())
                    .image(shop.getImage())
                    .street(shop.getStreet())
                    .district(shop.getDistrict())
                    .regency(shop.getRegency())
                    .province(shop.getProvince())
                    .postCode(shop.getPostCode())
                    .build();
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Gagal mengedit shop: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
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
            List<TransactionResponseShopDashboard> sortedTransactions = allTransaction.stream()
                    .filter(transaction -> transaction.getStatus() != null)
                    .map(transaction -> TransactionResponseShopDashboard.builder()
                            .referenceNumber(transaction.getTransactionNumber())
                            .createAt(transaction.getCreatedAt().toString())
                            .customerName(transaction.getCustomer().getName())
                            .startDate(transaction.getStartDate().toString())
                            .endDate(transaction.getEndDate().toString())
                            .duration(BigDecimal.valueOf(ChronoUnit.DAYS.between(transaction.getStartDate(), transaction.getEndDate()) + 1))
                            .status(transaction.getStatus())
                            .depositStatus(transaction.isDepositReturned())
                            .build())
                    .sorted(Comparator.comparing(TransactionResponseShopDashboard::getCreateAt).reversed())
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
