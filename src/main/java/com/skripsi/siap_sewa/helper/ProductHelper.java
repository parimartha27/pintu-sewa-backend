package com.skripsi.siap_sewa.helper;

import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ReviewEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.skripsi.siap_sewa.utils.CommonUtils.getRentDurationName;

public class ProductHelper {

    public static Double calculateWeightedRating(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double averageRating = reviews.stream()
                .mapToDouble(ReviewEntity::getRating)
                .average()
                .orElse(0.0);

        int numberOfReviews = reviews.size();

        double minimumVotes = 2.0;       
        double globalAverageRating = 3.5; 

        double weightedRating = (numberOfReviews / (numberOfReviews + minimumVotes)) * averageRating +
                (minimumVotes / (numberOfReviews + minimumVotes)) * globalAverageRating;

        return Math.round(weightedRating * 10) / 10.0;
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

    public static int[] countProductTransactions(Set<TransactionEntity> transactions) {
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

    public static int countRentedTimes(Set<TransactionEntity> transactions) {
        if (transactions == null) {
            return 0;
        }
        return (int) transactions.stream()
                .filter(t -> !t.isSelled())
                .count();
    }

    public static BigDecimal getLowestPrice(ProductEntity product) {
        return Stream.of(product.getDailyPrice(), product.getWeeklyPrice(), product.getMonthlyPrice())
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public static ProductResponse convertToResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
//                .category(product.getCategory())
                .rentCategory(getRentDurationName(product.getRentCategory()))
                .isRnb(product.isRnb())
                .image(product.getImage())
                .address(product.getShop() != null ? product.getShop().getRegency() : null)
                .rating(calculateWeightedRating(product.getReviews()))
                .rentedTimes(countRentedTimes(product.getTransactions()))
                .price(getLowestPrice(product))
                .build();
    }


}