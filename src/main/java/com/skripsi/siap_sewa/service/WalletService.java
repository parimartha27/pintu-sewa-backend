package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.wallet.WalletBalanceResponse;
import com.skripsi.siap_sewa.dto.wallet.WalletHistoryResponse;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.entity.WalletReportEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final CustomerRepository customerRepository;
    private final WalletReportRepository walletReportRepository;
    private final CommonUtils commonUtils;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final ShopRepository shopRepository;
    private final TransactionRepository transactionRepository;

    public ResponseEntity<ApiResponse> getWalletBalance(String id,String role) {
        try {
            WalletBalanceResponse response = new WalletBalanceResponse();
            if(role.equals("customer")){
                CustomerEntity customer = customerRepository.findById(id)
                        .orElseThrow(() -> new DataNotFoundException("Shop not found"));

                 response = WalletBalanceResponse.builder()
                        .balance(customer.getWalletAmount())
                        .build();
            }else if(role.equals("shop")){
                ShopEntity shop = shopRepository.findById(id)
                        .orElseThrow(() -> new DataNotFoundException("Shop not found"));

                 response = WalletBalanceResponse.builder()
                        .balance(shop.getBalance())
                        .build();
            }else{
                throw new DataNotFoundException("Role not found");
            }

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
        } catch (Exception ex) {
            log.error("Failed to get wallet balance: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<ApiResponse> getWalletHistory(String id, String role,int page, int size) {
        try {
            Page<WalletReportEntity> reports = null;

            if(role.equals("customer")){
                if (!customerRepository.existsById(id)) {
                    throw new DataNotFoundException("Customer not found");
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
                reports = walletReportRepository
                        .findByCustomerId(id, pageable);
            }else if(role.equals("shop")){
                if (!shopRepository.existsById(id)) {
                    throw new DataNotFoundException("Shop not found");
                }
                Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
                reports = walletReportRepository
                        .findByShopId(id, pageable);
            }else{
                throw new DataNotFoundException("Role not found");
            }

            // Convert to response
            List<WalletHistoryResponse.WalletHistory> history = reports.getContent().stream()
                    .map(this::convertToHistoryResponse)
                    .toList();

            WalletHistoryResponse response = WalletHistoryResponse.builder()
                    .walletHistory(history)
                    .build();

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
                .id(report.getId())
                .description(report.getDescription())
                .tanggalTransaksi(createAt.format(DATE_FORMATTER))
                .waktuTransaksi(waktu)
                .amount(report.getAmount())
                .isDebit(report.getType() == WalletReportEntity.WalletType.DEBIT)
                .build();
    }

    public ResponseEntity<ApiResponse> topUpWallet(String customerId, BigDecimal amount) {
        try {
            // Validate customer exists
            if (!customerRepository.existsById(customerId)) {
                throw new DataNotFoundException("Customer not found");
            }

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", customerId);
                        return new DataNotFoundException("Customer not found");
                    });

            customer.setWalletAmount(customer.getWalletAmount().add(amount));
            customer.setLastUpdateAt(LocalDateTime.now());
            customerRepository.save(customer);

            WalletReportEntity wallet = new WalletReportEntity();
            wallet.setDescription("Top Up Wallet ");
            wallet.setAmount(amount);
            wallet.setType(WalletReportEntity.WalletType.DEBIT);
            wallet.setCustomerId(customer.getId());
            wallet.setCreateAt(LocalDateTime.now());
            wallet.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(wallet);

            log.info("Successfully Top Up customer with ID: {}", customerId);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,"Top Up Berhasil");
        } catch (Exception ex) {
            log.error("Failed Top Up Customer : {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<ApiResponse> withdrawWallet(String shopId, BigDecimal amount) {
        try {
            // Validate customer exists
            if (!shopRepository.existsById(shopId)) {
                throw new DataNotFoundException("Shop not found");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Amount Must be greater than zero");
            }
            ShopEntity shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", shopId);
                        return new DataNotFoundException("Shop not found");
                    });

            shop.setBalance(shop.getBalance().subtract(amount));
            shop.setLastUpdateAt(LocalDateTime.now());
            shopRepository.save(shop);

            WalletReportEntity wallet = new WalletReportEntity();
            wallet.setDescription("Withdraw Wallet ");
            wallet.setAmount(amount);
            wallet.setType(WalletReportEntity.WalletType.CREDIT);
            wallet.setShopId(shop.getId());
            wallet.setCreateAt(LocalDateTime.now());
            wallet.setUpdateAt(LocalDateTime.now());
            walletReportRepository.save(wallet);

            log.info("Successfully Withdraw Shop with ID: {}", shopId);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,"Withdraw Berhasil");
        } catch (Exception ex) {
            log.error("Failed Withdraw Shop  : {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}