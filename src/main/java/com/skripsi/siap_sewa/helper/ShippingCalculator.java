package com.skripsi.siap_sewa.helper;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ShippingCalculator {

    private static final List<String> SHIPPING_PARTNERS = Arrays.asList(
            "JNE", "TIKI", "SiCepat", "J&T", "GoSend", "GrabExpress"
    );

    private static final List<BigDecimal> BASE_PRICES = Arrays.asList(
            BigDecimal.valueOf(15000), // JNE
            BigDecimal.valueOf(20000), // TIKI
            BigDecimal.valueOf(10000), // SiCepat
            BigDecimal.valueOf(12000), // J&T
            BigDecimal.valueOf(25000),  // GoSend
            BigDecimal.valueOf(30000)   // GrabExpress
    );

    private static final List<BigDecimal> PRICE_PER_KG = Arrays.asList(
            BigDecimal.valueOf(2000), // JNE
            BigDecimal.valueOf(2500), // TIKI
            BigDecimal.valueOf(1500), // SiCepat
            BigDecimal.valueOf(1800), // J&T
            BigDecimal.valueOf(3000), // GoSend
            BigDecimal.valueOf(3500)  // GrabExpress
    );

    public static ShippingInfo calculateShipping(BigDecimal totalWeight, ShopEntity shop, CustomerEntity customer) {
        Random random = new Random();
        int partnerIndex = random.nextInt(SHIPPING_PARTNERS.size());

        String partner = SHIPPING_PARTNERS.get(partnerIndex);
        BigDecimal basePrice = BASE_PRICES.get(partnerIndex);
        BigDecimal pricePerKg = PRICE_PER_KG.get(partnerIndex);

        // Calculate distance factor
        BigDecimal distanceFactor = calculateDistanceFactor(shop, customer);

        // Calculate shipping price
        BigDecimal weightPrice = pricePerKg.multiply(totalWeight);
        BigDecimal shippingPrice = basePrice.add(weightPrice).multiply(distanceFactor);

        // Calculate estimated time
        String estimatedTime = calculateEstimatedTime(distanceFactor);

        return new ShippingInfo(
                partner,
                shippingPrice.setScale(0, RoundingMode.HALF_UP),
                estimatedTime
        );
    }

    private static BigDecimal calculateDistanceFactor(ShopEntity shop, CustomerEntity customer) {
        // Same regency - 1.0x
        if (shop.getRegency().equalsIgnoreCase(customer.getRegency())) {
            return BigDecimal.valueOf(1.0);
        }
        // Same province - 1.5x
        else if (shop.getProvince().equalsIgnoreCase(customer.getProvince())) {
            return BigDecimal.valueOf(1.5);
        }
        // Different province - 2.5x
        else {
            return BigDecimal.valueOf(2.5);
        }
    }

    private static String calculateEstimatedTime(BigDecimal distanceFactor) {
        int baseDays = 1;
        if (distanceFactor.compareTo(BigDecimal.valueOf(1.5)) <= 0) {
            baseDays = 1;
        } else if (distanceFactor.compareTo(BigDecimal.valueOf(2.0)) <= 0) {
            baseDays = 2;
        } else {
            baseDays = 3;
        }
        return LocalDate.now().plusDays(baseDays).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }

    public record ShippingInfo(
            String partnerName,
            BigDecimal shippingPrice,
            String estimatedTime
    ) {}
}