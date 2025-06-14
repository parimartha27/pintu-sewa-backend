package com.skripsi.siap_sewa.helper;

import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ReviewEntity;
import com.skripsi.siap_sewa.entity.TransactionEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
        
        final BigDecimal ABSENT_PRICE = BigDecimal.ZERO;

        return Stream.of(product.getDailyPrice(), product.getWeeklyPrice(), product.getMonthlyPrice())
                .map(price -> Optional.ofNullable(price).orElse(ABSENT_PRICE))
                .filter(price -> price.compareTo(ABSENT_PRICE) != 0)
                .min(BigDecimal::compareTo)
                .orElse(ABSENT_PRICE);
    }

    public static ProductResponse convertToResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())

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