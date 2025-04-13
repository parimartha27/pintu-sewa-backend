package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.dto.customer.*;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.exception.EmailExistException;
import com.skripsi.siap_sewa.exception.PhoneNumberExistException;
import com.skripsi.siap_sewa.exception.UsernameExistException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CommonUtils commonUtils;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final JWTService jwtService;

    public ResponseEntity<ApiResponse> getCustomerDetails(String id) {
        try {
            log.info("Mengambil detail customer dengan ID: {}", id);

            Optional<CustomerEntity> customerEntity = customerRepository.findById(id);
            if(customerEntity.isEmpty()){
                log.warn("Customer tidak ditemukan dengan ID: {}", id);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            CustomerEntity data = customerEntity.get();
            CustomerDetailResponse response = objectMapper.convertValue(data, CustomerDetailResponse.class);
            response.setImage(data.getImage() == null ? "" : data.getImage());

            log.info("Berhasil mengambil detail customer dengan ID: {}", id);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Gagal mengambil detail customer: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> inputCustomerData(CreateNewCustomerRequest request) {
        try {
            log.info("Memproses input data customer baru: {}", request);

            Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());
            if (customerEntity.isEmpty()) {
                log.warn("Customer tidak ditemukan dengan ID: {}", request.getId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            CustomerEntity inputCustomerData = customerEntity.get();


            if (customerRepository.existsByUsername(request.getUsername())) {
                log.warn("Username sudah digunakan: {}", request.getUsername());
                throw new UsernameExistException("Username sudah digunakan");
            }

            if (request.getEmail() != null && !request.getEmail().equals(inputCustomerData.getEmail())) {
                if (customerRepository.existsByEmail(request.getEmail())) {
                    log.warn("Email sudah digunakan: {}", request.getEmail());
                    throw new EmailExistException("Email sudah digunakan");
                }
            }

            if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(inputCustomerData.getPhoneNumber())) {
                if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    log.warn("Nomor telepon sudah digunakan: {}", request.getPhoneNumber());
                    throw new PhoneNumberExistException("Nomor telepon sudah digunakan");
                }
            }

            // Set personal information
            inputCustomerData.setUsername(request.getUsername());
            inputCustomerData.setName(request.getName());
            inputCustomerData.setEmail(request.getEmail());
            inputCustomerData.setPhoneNumber(request.getPhoneNumber());
            inputCustomerData.setGender(request.getGender());
            inputCustomerData.setBirthDate(request.getBirthDate());
            inputCustomerData.setPassword(encoder.encode(request.getPassword()));
            inputCustomerData.setStatus("ACTIVE");

            // Clear OTP fields
            inputCustomerData.setOtp(null);
            inputCustomerData.setVerifyCount(0);
            inputCustomerData.setResendOtpCount(0);

            // Set address
            inputCustomerData.setStreet(request.getStreet());
            inputCustomerData.setDistrict(request.getDistrict());
            inputCustomerData.setRegency(request.getRegency());
            inputCustomerData.setProvince(request.getProvince());
            inputCustomerData.setPostCode(request.getPostCode());
            inputCustomerData.setLastUpdateAt(LocalDateTime.now());

            customerRepository.save(inputCustomerData);
            log.info("Berhasil menyimpan data customer baru dengan ID: {}", inputCustomerData.getId());

            CreateNewCustomerResponse response = CreateNewCustomerResponse.builder()
                    .customerId(inputCustomerData.getId())
                    .username(inputCustomerData.getUsername())
                    .phoneNumber(inputCustomerData.getPhoneNumber())
                    .email(inputCustomerData.getEmail())
                    .image(inputCustomerData.getImage() == null ? "" : inputCustomerData.getImage())
                    .status(inputCustomerData.getStatus())
                    .token(jwtService.generateToken(new CustomerPrincipal(inputCustomerData)))
                    .duration(1800)
                    .build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (UsernameExistException | EmailExistException | PhoneNumberExistException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal memproses input data customer: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editCustomerData(EditCustomerRequest request) {
        try {
            log.info("Memproses edit data customer dengan ID: {}", request.getId());

            Optional<CustomerEntity> customerEntity = customerRepository.findById(request.getId());
            if (customerEntity.isEmpty()) {
                log.warn("Customer tidak ditemukan dengan ID: {}", request.getId());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            CustomerEntity editedCustomerData = customerEntity.get();

            // Personal Information
            editedCustomerData.setGender(request.getGender());
            editedCustomerData.setBirthDate(request.getBirthDate());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                editedCustomerData.setPassword(encoder.encode(request.getPassword()));
            }

            // Address
            editedCustomerData.setStreet(request.getStreet());
            editedCustomerData.setDistrict(request.getDistrict());
            editedCustomerData.setRegency(request.getRegency());
            editedCustomerData.setProvince(request.getProvince());
            editedCustomerData.setPostCode(request.getPostCode());
            editedCustomerData.setLastUpdateAt(LocalDateTime.now());

            customerRepository.save(editedCustomerData);
            log.info("Berhasil mengupdate data customer dengan ID: {}", request.getId());

            CreateNewCustomerResponse response = objectMapper.convertValue(editedCustomerData, CreateNewCustomerResponse.class);
            response.setEmail(editedCustomerData.getEmail());
            response.setPhoneNumber(editedCustomerData.getPhoneNumber());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Gagal mengedit data customer: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> validateCredential(@Valid ValidateCredentialRequest request) {
        try {
            log.info("Validasi kredensial untuk email/telepon: {}", request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());

            boolean isCustomerValid = customerRepository.existsByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail());
            if(!isCustomerValid) {
                log.warn("Kredensial tidak valid untuk email/telepon: {}", request.getEmail() != null ? request.getEmail() : request.getPhoneNumber());
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "User tidak ditemukan");
            }

            Optional<CustomerEntity> validCustomer = customerRepository.findByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail());
            ValidateCredentialResponse response = ValidateCredentialResponse.builder()
                    .customerId(validCustomer.get().getId())
                    .build();

            log.info("Berhasil validasi kredensial untuk customer ID: {}", response.getCustomerId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Gagal validasi kredensial: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> forgetPassword(@Valid ForgetPasswordRequest request) {
        try {
            log.info("Memproses reset password untuk customer ID: {}", request.getCustomerId());

            Optional<CustomerEntity> optionalCustomer = customerRepository.findById(request.getCustomerId());
            if(optionalCustomer.isEmpty()){
                log.warn("Customer tidak ditemukan dengan ID: {}", request.getCustomerId());
                throw new DataNotFoundException("Customer dengan ID: " + request.getCustomerId() + " tidak ditemukan");
            }

            CustomerEntity updatedPassword = optionalCustomer.get();
            updatedPassword.setPassword(encoder.encode(request.getPassword()));
            customerRepository.save(updatedPassword);

            log.info("Berhasil reset password untuk customer ID: {}", request.getCustomerId());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Reset password berhasil");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gagal reset password: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getCustomerAddress(String customerId) {
        try {
            log.info("Fetching address for customer: {}", customerId);

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            AddressResponse response = AddressResponse.builder()
                    .customerId(customer.getId())
                    .street(customer.getStreet())
                    .district(customer.getDistrict())
                    .regency(customer.getRegency())
                    .province(customer.getProvince())
                    .postCode(customer.getPostCode())
                    .notes(customer.getNotes())
                    .build();

            log.info("Successfully fetched address for customer: {}", customerId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Error fetching customer address: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
}