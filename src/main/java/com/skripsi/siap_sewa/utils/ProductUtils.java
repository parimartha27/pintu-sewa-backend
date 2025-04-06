package com.skripsi.siap_sewa.utils;

import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ReviewEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductUtils {

    private final TransactionRepository transactionRepository;

    public static Double calculateMedianRating(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        List<Double> sortedRatings = reviews.stream()
                .map(ReviewEntity::getRating)
                .sorted()
                .collect(Collectors.toList());

        int size = sortedRatings.size();
        if (size % 2 == 0) {
            return (sortedRatings.get(size/2 - 1) + sortedRatings.get(size/2)) / 2.0;
        } else {
            return sortedRatings.get(size/2);
        }
    }

    public static String getTimeAgoInIndonesian(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + " tahun lalu";
        } else if (months > 0) {
            return months + " bulan lalu";
        } else if (days > 0) {
            return days + " hari lalu";
        } else if (hours > 0) {
            return hours + " jam lalu";
        } else if (minutes > 0) {
            return minutes + " menit lalu";
        } else {
            return "beberapa detik lalu";
        }
    }

    public static long countUniqueReviewers(List<ProductEntity> products) {
        return products.stream()
                .flatMap(product -> product.getReviews().stream())
                .map(review -> review.getCustomer().getId())
                .distinct()
                .count();
    }

    public static int[] countProductTransactions(List<TransactionEntity> transactions) {

        int rentedTimes = 0;
        int buyTimes = 0;

        for (TransactionEntity transaction : transactions) {
            if (transaction.isSelled()) {
                buyTimes++;
            } else {
                rentedTimes++;
            }
        }

        return new int[]{rentedTimes, buyTimes};
    }
}