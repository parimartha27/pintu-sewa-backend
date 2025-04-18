package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.admin.AdminRequest;
import com.skripsi.siap_sewa.utils.CommonUtils;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final CommonUtils commonUtils;
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final CustomerRepository customerRepository;

    public ResponseEntity<ApiResponse> loginAdmin(@Valid AdminRequest request) {
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
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, customerEntity.getFirst().getId());
    }
}
