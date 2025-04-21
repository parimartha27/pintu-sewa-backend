package com.skripsi.siap_sewa.dto.checkout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductCheckoutRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String productId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Min(1)
    private int quantity;

    private String shippingPartnerId;
}