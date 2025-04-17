package com.skripsi.siap_sewa.dto.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionResponse {
    private String referenceNumber;
    private String status;
    private String transactionDate;
    private ShopInfo shop;
    private List<ProductInfo> products;
    private BigDecimal totalPrice;
    private BigDecimal totalDeposit;
    private String shippingPartner;
    private BigDecimal shippingPrice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfo {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String orderId;  // Transaction ID (unique per product)
        private String productId;
        private String productName;
        private String image;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subTotal;
        private String startDate;
        private String endDate;
    }
}