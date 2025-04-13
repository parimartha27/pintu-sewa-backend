package com.skripsi.siap_sewa.dto.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductFilterRequest {
    private String category;
    private String name;
    private Integer rentDuration;
    private String location;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isRnb;
    private Integer minRating;
//    private String deliveryDuration;
    private String sortBy;
    private Sort.Direction sortDirection;
    private int page;
    private int size;

    public ProductFilterRequest() {
        this.sortBy = "name";
        this.sortDirection = Sort.Direction.ASC;
        this.page = 0;
        this.size = 16;
    }

    // Validate sortBy to ensure it only accepts valid fields
    public String getSortBy() {
        // Only allow sorting by name or price fields
        if (sortBy != null && (sortBy.equals("name") ||
                sortBy.equals("dailyPrice") ||
                sortBy.equals("weeklyPrice") ||
                sortBy.equals("monthlyPrice"))) {
            return sortBy;
        }
        // Default to name if invalid sort field is provided
        return "name";
    }
}