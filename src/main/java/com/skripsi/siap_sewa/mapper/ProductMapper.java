package com.skripsi.siap_sewa.mapper;

import com.skripsi.siap_sewa.dto.product.EditProductResponse;
import com.skripsi.siap_sewa.dto.product.ProductDetailResponse;
import com.skripsi.siap_sewa.dto.product.ProductListResponse;
import com.skripsi.siap_sewa.dto.product.ProductResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.helper.ProductHelper;
import com.skripsi.siap_sewa.utils.CommonUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(ProductEntity product) {
        if (product == null) return null;

        Double productRating = ProductHelper.calculateWeightedRating(product.getReviews());
        String address = (product.getShop() != null && product.getShop().getRegency() != null)
                ? product.getShop().getRegency()
                : "Kabupaten Tidak Diketahui";

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .rentCategory(CommonUtils.getRentDurationName(product.getRentCategory()))
                .isRnb(product.isRnb())
                .image(product.getImage())
                .address(address)
                .rating(productRating)
                .rentedTimes(ProductHelper.countRentedTimes(product.getTransactions()))
                .price(ProductHelper.getLowestPrice(product))
                .build();
    }

    public ProductDetailResponse toProductDetailResponse(ProductEntity product) {
        if (product == null) return null;

        List<String> images = processImageString(product.getImage());
        int[] transactionCounts = ProductHelper.countProductTransactions(product.getTransactions());
        Double rating = ProductHelper.calculateWeightedRating(product.getReviews());

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getRentCategory(),
                product.isRnb(),
                product.getWeight(),
                product.getHeight(),
                product.getWidth(),
                product.getLength(),
                product.getDailyPrice(),
                product.getWeeklyPrice(),
                product.getMonthlyPrice(),
                product.getDescription(),
                product.getConditionDescription(),
                product.getStock(),
                product.getMinRented(),
                product.getStatus(),
                images,
                rating,
                transactionCounts[0], 
                transactionCounts[1]  
        );
    }

    public ProductListResponse toProductListResponse(ProductEntity product) {
        if (product == null) return null;

        List<String> images = processImageString(product.getImage());
        Double rating = ProductHelper.calculateWeightedRating(product.getReviews());
        int[] transactionCounts = ProductHelper.countProductTransactions(product.getTransactions());
        String mainImage = images.isEmpty() ? null : images.get(0);

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .isRnb(product.isRnb())
                .dailyPrice(product.getDailyPrice())
                .weeklyPrice(product.getWeeklyPrice())
                .monthlyPrice(product.getMonthlyPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .mainImage(mainImage)
                .rating(rating)
                .rentedTimes(transactionCounts[0])
                .build();
    }

    public EditProductResponse toEditProductResponse(ProductEntity product) {
        if (product == null) return null;

        return EditProductResponse.builder()
                .name(product.getName())
                .category(product.getCategory())
                .rentCategory(product.getRentCategory())
                .isRnb(product.isRnb())
                .weight(product.getWeight())
                .height(product.getHeight())
                .width(product.getWidth())
                .length(product.getLength())
                .dailyPrice(product.getDailyPrice())
                .weeklyPrice(product.getWeeklyPrice())
                .monthlyPrice(product.getMonthlyPrice())
                .description(product.getDescription())
                .conditionDescription(product.getConditionDescription())
                .stock(product.getStock())
                .status(product.getStatus())
                .image(product.getImage())
                .shop(product.getShop())
                .build();
    }

    private List<String> processImageString(String imageString) {
        if (!StringUtils.hasText(imageString)) {
            return List.of(); 
        }
        return Arrays.stream(imageString.split(";"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}