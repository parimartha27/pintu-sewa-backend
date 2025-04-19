package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.admin.CustomerListResponse;
import com.skripsi.siap_sewa.dto.admin.ShopListResponse;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.entity.ShopEntity;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final CommonUtils commonUtils;
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;

    public ResponseEntity<ApiResponse> loginAdmin(@Valid AdminLoginRequest request) {
        List<CustomerEntity> customerEntity =
                customerRepository.findByUsername(request.getUsername());
        if(customerEntity.isEmpty()){
            log.warn("Admin not found : {}", request.getUsername());
            throw new DataNotFoundException("Admin not found");
        };

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        customerEntity.getFirst().getUsername(), request.getPassword()));

        if (!authentication.isAuthenticated()) {
            log.warn("Authentication Failed for Admin ");
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Failed to login");
        }
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, customerEntity.getFirst().getUsername());
    }

    public ResponseEntity<ApiResponse> getAllCustomers(int page) {
        try {

            log.info("Admin Fetching All Customer");

//          Create Object of Pageable
            Pageable pageable = PageRequest.of(page-1, 5);

//          Get All CustomerEntity By Pageable Object
            Page<CustomerEntity> allCustomers = customerRepository.findAll(pageable);

//          Condition If Customers is Empty
            if (allCustomers.isEmpty()) {
                log.info("There is No Customer found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

//          Mapping CustomerEntity into CustomerResponse
            List<CustomerListResponse> customerResponses = allCustomers.getContent().stream()
                    .map(entity -> CustomerListResponse.builder()
                            .customerId(entity.getId())
                            .username(entity.getUsername())
                            .email(entity.getEmail())
                            .phoneNumber(entity.getPhoneNumber())
                            .status(entity.getStatus())
                            .build())
                    .toList();

            PaginationResponse<CustomerListResponse> paginationResponse = createPaginationResponse(
                    customerResponses,
                    page,
                    5,
                    (int) allCustomers.getTotalElements(),
                    allCustomers.getTotalPages()
            );

            log.info("Successfully fetched {} Customers Data", allCustomers.getTotalElements());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.info("Error fetching all Customers Data : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getAllShops(int page) {
        try {

            log.info("Admin Fetching All Shops");

//          Create Object of Pageable
            Pageable pageable = PageRequest.of(page-1, 5);

//          Get All Shops By Pageable Object
            Page<ShopEntity> allShops = shopRepository.findAll(pageable);

//          Condition If Customers is Empty
            if (allShops.isEmpty()) {
                log.info("There is No Shops found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

//          Mapping CustomerEntity into CustomerResponse
            List<ShopListResponse> shopResponses = allShops.getContent().stream()
                    .map(entity -> ShopListResponse.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .description(entity.getDescription())
                            .street(entity.getStreet())
                            .regency(entity.getRegency())
                            .shopStatus(entity.getShopStatus())
                            .build())
                    .toList();

            PaginationResponse<ShopListResponse> paginationResponse = createPaginationResponse(
                    shopResponses,
                    page,
                    5,
                    (int) allShops.getTotalElements(),
                    allShops.getTotalPages()
            );

            log.info("Successfully fetched {} Customers Data", allShops.getTotalElements());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.info("Error fetching all Customers Data : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getAllReports() {
        try {

            log.info("Admin Fetching All Reports");

            Page<ShopEntity> allShops = shopRepository.findAll(pageable);

//          Condition If Customers is Empty
            if (allShops.isEmpty()) {
                log.info("There is No Shops found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

//          Mapping CustomerEntity into CustomerResponse
            List<ShopListResponse> shopResponses = allShops.getContent().stream()
                    .map(entity -> ShopListResponse.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .description(entity.getDescription())
                            .street(entity.getStreet())
                            .regency(entity.getRegency())
                            .shopStatus(entity.getShopStatus())
                            .build())
                    .toList();

            PaginationResponse<ShopListResponse> paginationResponse = createPaginationResponse(
                    shopResponses,
                    page,
                    5,
                    (int) allShops.getTotalElements(),
                    allShops.getTotalPages()
            );

            log.info("Successfully fetched {} Customers Data", allShops.getTotalElements());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.info("Error fetching all Customers Data : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

//  Function to Mapping Page Data Type into Pagination Response
    private <T> PaginationResponse<T>
    createPaginationResponse(List<T> Content, int page, int size, int totalElement, int totalPages) {
        return new PaginationResponse<>(
                Content,
                page,
                size,
                totalElement,
                totalPages
        );
    }
}
