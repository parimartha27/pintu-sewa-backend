package com.skripsi.siap_sewa.spesification;

import com.skripsi.siap_sewa.dto.product.ProductFilterRequest;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ProductSpecification {

    public static Specification<ProductEntity> withFilters(ProductFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add distinct if necessary to avoid duplicates
            query.distinct(true);

            // Name filter with improved search (case insensitive)
            if (StringUtils.hasText(filterRequest.getName())) {
                String searchTerm = "%" + filterRequest.getName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchTerm
                ));
            }

            // ShopId filter
            if (filterRequest.getShopId() != null) {
                Join<ProductEntity, ShopEntity> shopJoin = root.join("shop", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(shopJoin.get("id"), filterRequest.getShopId()));
            }

            // Category filter (exact match)
            if (!CollectionUtils.isEmpty(filterRequest.getCategories())) {
                predicates.add(root.get("category").in(filterRequest.getCategories()));
            }

            // Province filter (case insensitive)
            if (!CollectionUtils.isEmpty(filterRequest.getProvinces())) {
                Join<ProductEntity, ShopEntity> shopJoin = root.join("shop", JoinType.INNER);

                List<String> normalizedProvinces = filterRequest.getProvinces().stream()
                        .map(String::toLowerCase)
                        .toList();

                predicates.add(criteriaBuilder.lower(shopJoin.get("province")).in(normalizedProvinces));
            }

            // Rent duration filter (exact match)
            if (!CollectionUtils.isEmpty(filterRequest.getRentDurations())) {
                predicates.add(root.get("rentCategory").in(filterRequest.getRentDurations()));
            }

            // Price range filters
            addPricePredicates(filterRequest, root, criteriaBuilder, predicates);

            // IsRnb filter
            if (!CollectionUtils.isEmpty(filterRequest.getIsRnbOptions())) {
                predicates.add(root.get("isRnb").in(filterRequest.getIsRnbOptions()));
            }

            // Add additional indexes for these common queries
            query.orderBy(); // Let the pageable handle the ordering

            Predicate finalPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            log.debug("Generated filter predicate: {}", finalPredicate);
            return finalPredicate;
        };
    }

    private static void addPricePredicates(
            ProductFilterRequest filterRequest,
            Root<ProductEntity> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates) {

        BigDecimal minPrice = filterRequest.getMinPrice();
        BigDecimal maxPrice = filterRequest.getMaxPrice();

        if (minPrice != null && maxPrice != null) {
            // Both min and max price specified
            Predicate dailyPriceRange = criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("dailyPrice"), minPrice),
                    criteriaBuilder.lessThanOrEqualTo(root.get("dailyPrice"), maxPrice)
            );

            Predicate weeklyPriceRange = criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("weeklyPrice"), minPrice),
                    criteriaBuilder.lessThanOrEqualTo(root.get("weeklyPrice"), maxPrice)
            );

            Predicate monthlyPriceRange = criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("monthlyPrice"), minPrice),
                    criteriaBuilder.lessThanOrEqualTo(root.get("monthlyPrice"), maxPrice)
            );

            predicates.add(criteriaBuilder.or(dailyPriceRange, weeklyPriceRange, monthlyPriceRange));
        } else {
            // Only min price specified
            if (minPrice != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dailyPrice"), minPrice),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("weeklyPrice"), minPrice),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("monthlyPrice"), minPrice)
                ));
            }

            // Only max price specified
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.lessThanOrEqualTo(root.get("dailyPrice"), maxPrice),
                        criteriaBuilder.lessThanOrEqualTo(root.get("weeklyPrice"), maxPrice),
                        criteriaBuilder.lessThanOrEqualTo(root.get("monthlyPrice"), maxPrice)
                ));
            }
        }
    }
}
