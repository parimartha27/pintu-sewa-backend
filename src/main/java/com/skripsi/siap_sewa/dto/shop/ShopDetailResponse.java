package com.skripsi.siap_sewa.dto.shop;

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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopDetailResponse {
    private String id;
    private String name;
    private String description;
    private String email;
    private String shopStatus;
    private String image;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private String customerId;
    private Double rating;
    private int totalReviewedTimes;
    private List<ProductInfo> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ProductInfo {
        private String id;
        private String name;
        private String category;
        private int rentCategory;
        private boolean isRnb;
        private BigDecimal weight;
        private BigDecimal height;
        private BigDecimal width;
        private BigDecimal length;
        private BigDecimal dailyPrice;
        private BigDecimal weeklyPrice;
        private BigDecimal monthlyPrice;
        private String description;
        private String conditionDescription;
        private int stock;
        private int minRented;
        private String status;
        private String image;
        private Double rating;
        private int rentedTimes;
        private int buyTimes;
    }
}