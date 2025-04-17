package com.skripsi.siap_sewa.dto.cart;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
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
public class AddCartRequest {

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;

    @NotBlank(message = "Product ID tidak boleh kosong")
    private String productId;

    @Min(value = 1, message = "Quantity harus minimal 1")
    private int quantity;

    @NotNull(message = "Start rent date tidak boleh kosong")
    private LocalDate startRentDate;

    @NotNull(message = "End rent date tidak boleh kosong")
    @FutureOrPresent(message = "End rent date harus di masa depan atau saat ini")
    private LocalDate endRentDate;

    @AssertTrue(message = "End rent date harus setelah start rent date")
    public boolean isValidRentalPeriod() {
        return startRentDate != null && endRentDate != null &&
                (endRentDate.isAfter(startRentDate) || endRentDate.isEqual(startRentDate));
    }
}
