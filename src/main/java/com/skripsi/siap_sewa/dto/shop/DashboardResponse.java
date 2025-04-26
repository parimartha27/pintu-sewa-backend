package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.entity.WalletReportEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DashboardResponse {
    private List<TransactionEntity> TransactionList;
    private BigDecimal wallet;
    private double averageRating;
    private String shopStatus;
    private int TransactionCount;
    private Page<WalletReportEntity> walletReport;

}