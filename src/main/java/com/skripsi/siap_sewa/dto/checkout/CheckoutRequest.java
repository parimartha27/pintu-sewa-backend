package com.skripsi.siap_sewa.dto.checkout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.lang.Nullable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutRequest {

    @NotBlank
    private String customerId;

    @Nullable
    private String productId;

    @Nullable
    private LocalDate startDate;

    @Nullable
    private LocalDate endDate;

    @Nullable
    @Min(1)
    private Integer quantity;

    @Nullable
    private String cartId;

    public boolean isCartCheckout() {
        return cartId != null;
    }

    public boolean isProductDetailCheckout() {
        return productId != null && startDate != null && endDate != null && quantity != null;
    }
}