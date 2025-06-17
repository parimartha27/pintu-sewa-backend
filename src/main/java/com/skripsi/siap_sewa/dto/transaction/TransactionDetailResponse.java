package com.skripsi.siap_sewa.dto.transaction;

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
public class TransactionDetailResponse {
    private TransactionDetail transactionDetail;
    private List<ProductDetail> productDetails;
    private PaymentDetail paymentDetail;
    private TransactionResponse.ShopInfo shopDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TransactionDetail {
        private String referenceNumber;
        private String status;
        private String transactionDate;
        private String shippingAddress;
        private String shippingPartner;
        private String shippingCode;
        private String returnCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductDetail {
        private String orderId;
        private String productId;
        private String productName;
        private String image;
        private String startRentDate;
        private String endRentDate;
        private int quantity;
        private BigDecimal price;
        private BigDecimal subTotal;
        private BigDecimal deposit;
        private ShopInfo shop;
        private BigDecimal buyProductPrice;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ShopInfo {
            private String id;
            private String name;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentDetail {
        private String paymentMethod;
        private BigDecimal subTotal;
        private BigDecimal shippingPrice;
        private BigDecimal serviceFee;
        private BigDecimal totalDeposit;
        private BigDecimal grandTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShopDetail {
        private String id;
        private String name;
    }
}
