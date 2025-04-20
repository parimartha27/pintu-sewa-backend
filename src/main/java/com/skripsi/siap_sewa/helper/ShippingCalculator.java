package com.skripsi.siap_sewa.helper;

import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShippingCalculator {

    private static final List<ShippingPartner> SHIPPING_PARTNERS = List.of(
            new ShippingPartner("1", "JNE", new BigDecimal("15000"), new BigDecimal("2000")),
            new ShippingPartner("2", "TIKI", new BigDecimal("20000"), new BigDecimal("2500")),
            new ShippingPartner("3", "SiCepat", new BigDecimal("10000"), new BigDecimal("1500")),
            new ShippingPartner("4", "J&T", new BigDecimal("12000"), new BigDecimal("1800")),
            new ShippingPartner("5", "GoSend", new BigDecimal("25000"), new BigDecimal("3000")),
            new ShippingPartner("6", "GrabExpress", new BigDecimal("30000"), new BigDecimal("3500"))
    );

    public static ShippingInfo calculateShipping(BigDecimal totalWeight,
                                                 ShopEntity shop, CustomerEntity customer, String shippingPartnerName) {

        ShippingPartner partner = SHIPPING_PARTNERS.stream()
                .filter(p -> p.getName().equals(shippingPartnerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Shipping partner not found"));

        // Calculate distance factor
        BigDecimal distanceFactor = calculateDistanceFactor(shop, customer);

        // Calculate shipping price
        BigDecimal weightPrice = partner.getPricePerKg().multiply(totalWeight);
        BigDecimal shippingPrice = partner.getBasePrice().add(weightPrice).multiply(distanceFactor);

        // Calculate estimated time
        String estimatedTime = calculateEstimatedTime(distanceFactor);

        return new ShippingInfo(
                partner.getName(),
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

    @Getter
    @AllArgsConstructor
    private static class ShippingPartner {
        private String id;
        private String name;
        private BigDecimal basePrice;
        private BigDecimal pricePerKg;
    }
}