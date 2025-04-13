package com.skripsi.siap_sewa.spesification;

import com.skripsi.siap_sewa.dto.product.ProductFilterRequest;
import com.skripsi.siap_sewa.entity.ProductEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<ProductEntity> withFilters(ProductFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Name filter (case insensitive contains)
            if (StringUtils.hasText(filterRequest.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filterRequest.getName().toLowerCase() + "%"
                ));
            }

            // Category filter
            if (StringUtils.hasText(filterRequest.getCategory())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        filterRequest.getCategory().toLowerCase()
                ));
            }

            // Location/regency filter
            if (StringUtils.hasText(filterRequest.getLocation())) {
                Join<ProductEntity, Object> shopJoin = root.join("shop");
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(shopJoin.get("regency")),
                        filterRequest.getLocation().toLowerCase()
                ));
            }

            // Rent duration filter
            if (filterRequest.getRentDuration() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("rentCategory"),
                        filterRequest.getRentDuration()
                ));
            }

            // Price range filters
            if (filterRequest.getMinPrice() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dailyPrice"), filterRequest.getMinPrice()),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("weeklyPrice"), filterRequest.getMinPrice()),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("monthlyPrice"), filterRequest.getMinPrice())
                ));
            }

            if (filterRequest.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.lessThanOrEqualTo(root.get("dailyPrice"), filterRequest.getMaxPrice()),
                        criteriaBuilder.lessThanOrEqualTo(root.get("weeklyPrice"), filterRequest.getMaxPrice()),
                        criteriaBuilder.lessThanOrEqualTo(root.get("monthlyPrice"), filterRequest.getMaxPrice())
                ));
            }

            // isRnb filter
            if (filterRequest.getIsRnb() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("isRnb"),
                        filterRequest.getIsRnb()
                ));
            }

            // Combine all predicates with AND
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
