package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.wallet.WalletBalanceResponse;
import com.skripsi.siap_sewa.dto.wallet.WalletHistoryResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.WalletReportEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.WalletReportRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final CustomerRepository customerRepository;
    private final WalletReportRepository walletReportRepository;
    private final CommonUtils commonUtils;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ResponseEntity<ApiResponse> getWalletBalance(String customerId) {
        try {
            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new DataNotFoundException("Customer not found"));

            WalletBalanceResponse response = WalletBalanceResponse.builder()
                    .balance(customer.getWalletAmount())
                    .build();

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Failed to get wallet balance: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<ApiResponse> getWalletHistory(String customerId, int page, int size) {
        try {
            // Validate customer exists
            if (!customerRepository.existsById(customerId)) {
                throw new DataNotFoundException("Customer not found");
            }

            // Get paginated wallet history
            Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
            Page<WalletReportEntity> reports = walletReportRepository
                    .findByCustomerId(customerId, pageable);

            // Convert to response
            List<WalletHistoryResponse.WalletHistory> history = reports.getContent().stream()
                    .map(this::convertToHistoryResponse)
                    .toList();

            WalletHistoryResponse response = WalletHistoryResponse.builder()
                    .walletHistory(history)
                    .build();

//            // Add pagination info
//            Map<String, Object> meta = new HashMap<>();
//            meta.put("page", reports.getNumber());
//            meta.put("size", reports.getSize());
//            meta.put("totalItems", reports.getTotalElements());
//            meta.put("totalPages", reports.getTotalPages());

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Failed to get wallet history: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private WalletHistoryResponse.WalletHistory convertToHistoryResponse(WalletReportEntity report) {
        LocalDateTime createAt = report.getCreateAt();
        String waktu = createAt.format(TIME_FORMATTER) + " WIB"; // Adjust timezone if needed

        return WalletHistoryResponse.WalletHistory.builder()
                .description(report.getDescription())
                .tanggalTransaksi(createAt.format(DATE_FORMATTER))
                .waktuTransaksi(waktu)
                .amount(report.getAmount())
                .isDebit(report.getType() == WalletReportEntity.WalletType.DEBIT)
                .build();
    }
}