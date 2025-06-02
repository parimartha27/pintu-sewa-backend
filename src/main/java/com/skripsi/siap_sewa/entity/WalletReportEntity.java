package com.skripsi.siap_sewa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "WALLET_REPORT")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String customerId;

    private String shopId;
    @Column(precision = 38, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WalletType type; // DEBIT or CREDIT

    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public enum WalletType {
        DEBIT, CREDIT
    }
}
