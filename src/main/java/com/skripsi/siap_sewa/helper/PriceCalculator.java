package com.skripsi.siap_sewa.helper;

import com.skripsi.siap_sewa.entity.ProductEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PriceCalculator {

    public static RentalPrice calculateRentalPrice(ProductEntity product, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        BigDecimal dailyPrice = product.getDailyPrice();
        BigDecimal weeklyPrice = product.getWeeklyPrice();
        BigDecimal monthlyPrice = product.getMonthlyPrice();

        BigDecimal totalPrice = BigDecimal.ZERO;

        // Calculate based on monthly, weekly, daily chunks
        long months = days / 30;
        days = days % 30;

        long weeks = days / 7;
        days = days % 7;

        totalPrice = totalPrice.add(monthlyPrice.multiply(BigDecimal.valueOf(months)));
        totalPrice = totalPrice.add(weeklyPrice.multiply(BigDecimal.valueOf(weeks)));
        totalPrice = totalPrice.add(dailyPrice.multiply(BigDecimal.valueOf(days)));

        return new RentalPrice(
                totalPrice.setScale(0, RoundingMode.HALF_UP),
                formatDuration(months, weeks, days)
        );
    }

    private static String formatDuration(long months, long weeks, long days) {
        StringBuilder sb = new StringBuilder();
        if (months > 0) sb.append(months).append(months == 1 ? " Month " : " Months ");
        if (weeks > 0) sb.append(weeks).append(weeks == 1 ? " Week " : " Weeks ");
        if (days > 0) sb.append(days).append(days == 1 ? " Day" : " Days");
        return sb.toString().trim();
    }

    public record RentalPrice(
            BigDecimal totalPrice,
            String durationText
    ) {}
}
