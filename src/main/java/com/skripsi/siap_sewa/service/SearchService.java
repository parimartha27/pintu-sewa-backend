package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.SearchResponse;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CommonUtils commonUtils;

    public ResponseEntity<ApiResponse> searchShopAndProduct(String keyword) {
        try {
            log.info("Searching for shops and products with keyword: {}", keyword);

            List<ProductEntity> products = productRepository.searchProducts(keyword);
            List<ShopEntity> shops = shopRepository.searchShops(keyword);

            if (products.isEmpty() && shops.isEmpty()) {
                log.info("No exact matches found, searching for similar items");

                products = productRepository.findSimilarProductsByCategory(keyword);
                shops = shopRepository.findSimilarShopsByName(keyword);
            }

            SearchResponse response = mapToResponse(products, shops);

            log.info("Search completed successfully for keyword: {}", keyword);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (Exception ex) {
            log.error("Error processing search for keyword {}: {}", keyword, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private SearchResponse mapToResponse(List<ProductEntity> products, List<ShopEntity> shops) {
        List<SearchResponse.ProductItem> productItems = products.stream()
                .limit(3)
                .map(product -> SearchResponse.ProductItem.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .build())
                .toList();

        List<SearchResponse.ShopItem> shopItems = shops.stream()
                .limit(3)
                .map(shop -> SearchResponse.ShopItem.builder()
                        .id(shop.getId())
                        .name(shop.getName())
                        .build())
                .toList();

        return SearchResponse.builder()
                .products(productItems)
                .shops(shopItems)
                .build();
    }
}