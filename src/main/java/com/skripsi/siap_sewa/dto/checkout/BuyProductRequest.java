package com.skripsi.siap_sewa.dto.checkout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BuyProductRequest {
    private String customerId;
    private String productId;
    private String referenceNumber;
    private String transactionId;
    private BigDecimal amountPayment;
    private String paymentMethod;
}
