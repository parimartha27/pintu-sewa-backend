package com.skripsi.siap_sewa.dto.cart;

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
public class CartResponse {

    private String shopId;
    private String shopName;
    private List<CartInfo> carts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CartInfo {
        private String cartId;
        private String productId;
        private String productName;
        private BigDecimal price;
        private String startRentDate;
        private String endRentDate;
        private String rentDuration;
        private int quantity;
        private boolean isAvailableToRent;
        private String image;
        private int stock;
    }
}
