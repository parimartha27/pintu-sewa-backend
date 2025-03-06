package com.skripsi.siap_sewa.dto.shop;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopResponse {

    private String name;
    private String description;
    private String instagram;
    private String facebook;
    private int shopStatus;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;
    private CustomerEntity customer;
    private List<ProductEntity> products;
}
