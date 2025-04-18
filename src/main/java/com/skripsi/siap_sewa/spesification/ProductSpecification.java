package com.skripsi.siap_sewa.spesification;

import com.skripsi.siap_sewa.dto.product.ProductFilterRequest;
import com.skripsi.siap_sewa.entity.ProductEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSpecification {

    public static Specification<ProductEntity> withFilters(ProductFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filterRequest.getShopId())) {
                Join<Object, Object> shopJoin = root.join("shop");
                predicates.add(criteriaBuilder.equal(shopJoin.get("id"), filterRequest.getShopId()));
            }

            // Name filter (case insensitive contains)
            if (StringUtils.hasText(filterRequest.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filterRequest.getName().toLowerCase() + "%"
                ));
            }

            // Category filter (multiple values)
            if (filterRequest.getCategories() != null && !filterRequest.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(
                        filterRequest.getCategories().stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList())
                ));
            }

            // Location/regency filter (multiple values)
            if (filterRequest.getLocations() != null && !filterRequest.getLocations().isEmpty()) {
                Join<ProductEntity, Object> shopJoin = root.join("shop");
                predicates.add(shopJoin.get("regency").in(
                        filterRequest.getLocations().stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList())
                ));
            }

            // Rent duration filter (multiple values)
            if (filterRequest.getRentDurations() != null && !filterRequest.getRentDurations().isEmpty()) {
                predicates.add(root.get("rentCategory").in(filterRequest.getRentDurations()));
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

            // isRnb filter (multiple values)
            if (filterRequest.getIsRnbOptions() != null && !filterRequest.getIsRnbOptions().isEmpty()) {
                predicates.add(root.get("isRnb").in(filterRequest.getIsRnbOptions()));
            }

            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
