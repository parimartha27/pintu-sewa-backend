package com.skripsi.siap_sewa.dto.cart;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CartResponse {

    private String productId;
    private ProductResponse product;
    private int quantity;
    private BigDecimal totalAmount;
    private LocalDate startRentDate;
    private LocalDate endRentDate;
    private String street;
    private String district;
    private String regency;
    private String province;
    private String postCode;
    private String notes;
}
