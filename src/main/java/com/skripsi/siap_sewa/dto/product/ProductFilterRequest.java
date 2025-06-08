package com.skripsi.siap_sewa.dto.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductFilterRequest {
    private List<String> categories;
    private String name;
    private List<Integer> rentDurations;
    private List<String> provinces;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<Boolean> isRnbOptions;
    private List<Double> minRatings;
    private String sortBy;
    private Sort.Direction sortDirection;
    private int page;
    private int size;
    private String shopId;

    public void validate() {
        // Validate pagination parameters
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be greater than or equal to 1");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        // Validate rent durations
        if (rentDurations != null) {
            for (Integer duration : rentDurations) {
                if (duration < 1 || duration > 7) {
                    throw new IllegalArgumentException("Invalid rent duration value: " + duration);
                }
            }
        }

        // Validate ratings
        if (minRatings != null) {
            for (Double rating : minRatings) {
                if (rating < 0 || rating > 5) {
                    throw new IllegalArgumentException("Rating must be between 0 and 5");
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                categories == null ? 0 : new HashSet<>(categories),
                name,
                rentDurations == null ? 0 : new HashSet<>(rentDurations),
                provinces == null ? 0 : new HashSet<>(provinces),
                minPrice,
                maxPrice,
                isRnbOptions == null ? 0 : new HashSet<>(isRnbOptions),
                minRatings == null ? 0 : new HashSet<>(minRatings),
                sortBy,
                sortDirection
        );
    }
}