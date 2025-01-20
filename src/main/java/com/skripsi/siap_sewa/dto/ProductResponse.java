package com.skripsi.siap_sewa.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductResponse {

    private String name;
    private BigDecimal priceInDay;
    private BigDecimal priceInWeek;
    private BigDecimal priceInMonth;
    private int stock;
    private Boolean isRentToBuy;
    private Boolean isDelete;
    private int minimumRentDay;
    private int minimumRentQuantity;
    private int maxQuantityToRent;
    private String image;

}