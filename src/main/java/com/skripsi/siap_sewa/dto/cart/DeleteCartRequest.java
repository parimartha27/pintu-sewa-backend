package com.skripsi.siap_sewa.dto.cart;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeleteCartRequest {

    @NotBlank(message = "Cart ID tidak boleh kosong")
    private String cartId;

    @NotBlank(message = "Customer ID tidak boleh kosong")
    private String customerId;
}
