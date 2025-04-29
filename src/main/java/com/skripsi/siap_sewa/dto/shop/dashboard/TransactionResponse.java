package com.skripsi.siap_sewa.dto.shop.dashboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TransactionResponse {
    private String refferenceNo;
    private String createAt;
    private String customerName;
    private String startDate;
    private String endDate;
    private BigDecimal duration;
    private String status;
    private boolean depositStatus;
}
