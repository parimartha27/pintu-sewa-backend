package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminLoginRequest;
import com.skripsi.siap_sewa.dto.admin.CustomerListResponse;
import com.skripsi.siap_sewa.dto.admin.DashboardResponse;
import com.skripsi.siap_sewa.dto.admin.ShopListResponse;
import com.skripsi.siap_sewa.dto.customer.CustomerDetailResponse;
import com.skripsi.siap_sewa.dto.customer.EditBiodataRequest;
import com.skripsi.siap_sewa.dto.customer.EditCustomerRequest;
import com.skripsi.siap_sewa.dto.product.PaginationResponse;
import com.skripsi.siap_sewa.dto.shop.EditShopRequest;
import com.skripsi.siap_sewa.dto.shop.EditShopResponse;
import com.skripsi.siap_sewa.entity.CartEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.exception.PhoneNumberExistException;
import com.skripsi.siap_sewa.repository.ChatRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final CommonUtils commonUtils;
    private final AuthenticationManager authManager;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ChatRepository chatRepository;

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

    public ResponseEntity<ApiResponse> viewDashboard() {
        try{
            log.info("Get Count Of All Data");
            DashboardResponse dashboardResponse = DashboardResponse.builder()
                    .customersCount((int) customerRepository.count())
                    .shopsCount((int) shopRepository.count())
                    .reportsCount((int) chatRepository.count())
                    .build();

            log.info("Dashboard Data Fetched: {}", dashboardResponse);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, dashboardResponse);
        } catch (Exception ex) {
            log.error("Fetching Dashboard Data Failed : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
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

    public ResponseEntity<ApiResponse> editBiodata(EditCustomerRequest request) {
        try {
            log.info("Process Admin Edit Biodata Customer dengan ID: {}", request.getId());

            Optional<CustomerEntity> customerOpt = customerRepository.findById(request.getId());
            if (customerOpt.isEmpty()) {
                log.info("Customer tidak ditemukan dengan ID: {}", request.getId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            CustomerEntity customer = customerOpt.get();

            // Validasi username
            if (!customer.getUsername().equals(request.getUsername())) {
                // Cek unik (kecuali milik sendiri)
                if (customerRepository.existsByUsernameAndIdNot(request.getUsername(), request.getId())) {
                    log.info("Username {} sudah digunakan", request.getUsername());
                    return commonUtils.setResponse(ErrorMessageEnum.USERNAME_EXIST, null);
                }
            }

            // Validasi email unik (kecuali milik user ini)
            if (!customer.getEmail().equals(request.getEmail())) {
                if (customerRepository.existsByEmailAndIdNot(request.getEmail(), request.getId())) {
                    log.info("Email {} sudah digunakan", request.getEmail());
                    return commonUtils.setResponse(ErrorMessageEnum.EMAIL_EXIST, null);
                }
            }

            if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(customer.getPhoneNumber())) {
                if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    log.info("Nomor telepon sudah digunakan: {}", request.getPhoneNumber());
                    throw new PhoneNumberExistException("Nomor telepon sudah digunakan");
                }
            }

            // Update semua field biodata
            customer.setUsername(request.getUsername());
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setGender(request.getGender());
            customer.setBirthDate(request.getBirthDate());
            customer.setImage(request.getImage());
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, customer.getId());

        } catch (Exception ex) {
            log.info("Admin Gagal mengedit biodata customer: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> suspendCustomer(String id) {
        try {
            log.info("Admin Attempting to Suspend Customer with ID: {}", id);

            CustomerEntity customer = customerRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", id);
                        return new DataNotFoundException("Customer not found");
                    });


            customer.setStatus("SUSPENDED");
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);

            log.info("Successfully Suspend customer with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Customer Suspend successfully");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error Suspend Customer with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> unSuspendCustomer(String id) {
        try {
            log.info("Admin Attempting to Suspend Customer with ID: {}", id);

            CustomerEntity customer = customerRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", id);
                        return new DataNotFoundException("Customer not found");
                    });


            customer.setStatus("ACTIVE");
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);

            log.info("Successfully Suspend customer with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Customer Suspend successfully");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error Suspend Customer with ID {}: {}", id, ex.getMessage(), ex);
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
                            .customerName(entity.getCustomer().getUsername())
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
            log.info("Error fetching all Shops Data : {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editShop(@Valid EditShopRequest request) {
        Optional<ShopEntity> shopEntity = shopRepository.findById(request.getId());

        if(shopEntity.isPresent()) {
            ShopEntity shop = shopEntity.get();

            shop.setName(request.getName());
            shop.setDescription(request.getDescription());
            shop.setImage(request.getImage());
            shop.setStreet(request.getStreet());
            shop.setDistrict(request.getDistrict());
            shop.setRegency(request.getRegency());
            shop.setProvince(request.getProvince());
            shop.setPostCode(request.getPostCode());

            shopRepository.save(shop);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, shop.getId());
        }

        return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
    }

    public ResponseEntity<ApiResponse> deactiveShop(String id) {
        try {
            log.info("Admin Attempting to Deactive Shop with ID: {}", id);

            ShopEntity shop = shopRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", id);
                        return new DataNotFoundException("Customer not found");
                    });


            shop.setShopStatus("DEACTIVE");
            shop.setLastUpdateAt(LocalDateTime.now());
            shopRepository.save(shop);

            log.info("Successfully Deactive Shop with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Deactive Shop successfully");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error Deactive Shop with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> activeShop(String id) {
        try {
            log.info("Admin Attempting to Activate Shop with ID: {}", id);

            ShopEntity shop = shopRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", id);
                        return new DataNotFoundException("Customer not found");
                    });


            shop.setShopStatus("ACTIVE");
            shop.setLastUpdateAt(LocalDateTime.now());
            shopRepository.save(shop);

            log.info("Successfully Activate Shop with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Activate Shop successfully");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error Activate Shop with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> adminLogout(String id) {
        try {
            CustomerEntity adminData =
                    customerRepository.findById(id)
                                      .orElseThrow(() -> new DataNotFoundException("Data Admin Not Found"));
            adminData.setLastLogin(LocalDateTime.now());
            customerRepository.save(adminData);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, null);
        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Logout Admin Failed : {}", ex.getMessage(), ex);
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
