package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skripsi.siap_sewa.dto.shop.dashboard.TransactionResponseShopDashboard;
import com.skripsi.siap_sewa.dto.shop.dashboard.WalletReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DashboardResponse {
    private List<TransactionResponseShopDashboard> TransactionList;
    private BigDecimal wallet;
    private double averageRating;
    private String shopStatus;
    private int TransactionCount;
    private List<WalletReportResponse> walletReport;

}