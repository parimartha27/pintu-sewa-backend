package com.skripsi.siap_sewa.spesification;

import com.skripsi.siap_sewa.dto.transaction.ShopTransactionFilterRequest;
import com.skripsi.siap_sewa.dto.transaction.TransactionFilterRequest;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<TransactionEntity> withFilters(TransactionFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by customer ID
            if (StringUtils.hasText(filterRequest.getCustomerId())) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), filterRequest.getCustomerId()));
            }

            // Filter by status
            if (StringUtils.hasText(filterRequest.getStatus()) && !"semua".equalsIgnoreCase(filterRequest.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            // Search by reference number, shop name or product name
            if (StringUtils.hasText(filterRequest.getSearchQuery())) {
                String searchPattern = "%" + filterRequest.getSearchQuery().toLowerCase() + "%";

                Predicate refNumberPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("transactionNumber")), searchPattern);

                Predicate shopNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("products").join("shop").get("name")), searchPattern);

                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("products").get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(refNumberPredicate, shopNamePredicate, productNamePredicate));
            }

            // Filter by date range
            if (filterRequest.getStartDate() != null) {
                LocalDateTime startOfDay = filterRequest.getStartDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
            }

            if (filterRequest.getEndDate() != null) {
                LocalDateTime endOfDay = filterRequest.getEndDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<TransactionEntity> withFiltersShop(ShopTransactionFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by Shop Id
            if (StringUtils.hasText(filterRequest.getShopId())) {
                predicates.add(criteriaBuilder.equal(root.get("shopId"), filterRequest.getShopId()));
            }

            // Filter by Status
            if (StringUtils.hasText(filterRequest.getStatus()) && !"semua".equalsIgnoreCase(filterRequest.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            // Search by reference number, shop name or product name
            if (StringUtils.hasText(filterRequest.getSearchQuery())) {
                String searchPattern = "%" + filterRequest.getSearchQuery().toLowerCase() + "%";

                Predicate refNumberPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("transactionNumber")), searchPattern);

                Predicate shopNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("customer").get("name")), searchPattern);

                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("products").get("name")), searchPattern);

                predicates.add(criteriaBuilder.or(refNumberPredicate, shopNamePredicate, productNamePredicate));
            }

            // Filter by date range
            if (filterRequest.getStartDate() != null) {
                LocalDateTime startOfDay = filterRequest.getStartDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
            }

            if (filterRequest.getEndDate() != null) {
                LocalDateTime endOfDay = filterRequest.getEndDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}