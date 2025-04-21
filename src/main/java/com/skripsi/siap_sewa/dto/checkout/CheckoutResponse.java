package com.skripsi.siap_sewa.dto.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CheckoutResponse {

    private List<TransactionGroup> transactions;
    private BigDecimal subTotalProductPrice;
    private BigDecimal subTotalShippingCost;
    private BigDecimal subTotalDeposit;
    private BigDecimal serviceFee;
    private BigDecimal grandTotalPayment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TransactionGroup {
        private String shopId;
        private String shopName;
        private List<RentedItem> rentedItems;
        private BigDecimal deposit;
        private String shippingPartner;
        private BigDecimal shippingPrice;
        private int totalRentedProduct;
        private BigDecimal totalPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RentedItem {
        private String transactionId;
        private String productId;
        private String productName;
        private BigDecimal price;
        private String startRentDate;
        private String endRentDate;
        private String rentDuration;
        private int quantity;
        private boolean availableToRent;
    }
}