package com.skripsi.siap_sewa.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchResponse {
    private List<ProductItem> products;
    private List<ShopItem> shops;

    @Data
    @Builder
    public static class ProductItem {
        private String id;
        private String name;
    }

    @Data
    @Builder
    public static class ShopItem {
        private String id;
        private String name;
    }
}
