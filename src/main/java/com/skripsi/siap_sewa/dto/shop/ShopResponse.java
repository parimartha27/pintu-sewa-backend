package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skripsi.siap_sewa.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopResponse {

    private String name;
    private String description;
    private String shopStatus;
    private String image;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;
    private String customerId;
    private List<ProductEntity> products;
}
