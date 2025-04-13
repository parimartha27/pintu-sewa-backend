// TransactionResponse.java
package com.skripsi.siap_sewa.dto.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionResponse {
    private String orderId;
    private String status;
    private String transactionDate;
    private String referenceNumber;
    private ShopInfo shop;
    private List<ProductInfo> products;
    private BigDecimal totalPrice;

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
        private String id;
        private String name;
        private Integer quantity;
        private BigDecimal price;
    }
}