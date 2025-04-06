package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.ProductDetailResponse;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import com.skripsi.siap_sewa.utils.ProductUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ObjectMapper objectMapper;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;

    public ResponseEntity<ApiResponse> getProductsByCategory(String category, Pageable pageable) {
        try {
            log.info("Fetching products by category: {}", category);

            Page<ProductEntity> productPage = productRepository.findByCategory(category, pageable);

            if (productPage.isEmpty()) {
                log.warn("No products found for category: {}", category);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductResponse> responseList = productPage.getContent().stream()
                    .map(this::buildProductResponse)
                    .collect(Collectors.toList());

            PaginationResponse<ProductResponse> paginationResponse = new PaginationResponse<>(
                    responseList,
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages()
            );

            log.debug("Successfully fetched {} products for category: {}", responseList.size(), category);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, paginationResponse);

        } catch (Exception ex) {
            log.error("Error fetching products by category {}: {}", category, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductDetail(String id) {
        try {
            log.info("Fetching product details for ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", id);
                        return new DataNotFoundException("Product not found");
                    });

            ProductDetailResponse response = mapProductToDetailResponse(product);

            log.debug("Successfully fetched product details for ID: {}", id);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching product details for ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ProductDetailResponse mapProductToDetailResponse(ProductEntity product) {
        ProductDetailResponse response = modelMapper.map(product, ProductDetailResponse.class);

        // Set median rating for product
        Double medianRating = ProductUtils.calculateMedianRating(product.getReviews());
        response.setRating(medianRating);

        // Set transaction counts
        int[] transactionCounts = countProductTransactions(product.getId());
        response.setRentedTimes(transactionCounts[0]);
        response.setBuyTimes(transactionCounts[1]);

        // Map shop info with median rating and total reviewers
        response.setShop(mapShopToShopInfo(product.getShop()));

        // Map reviews with time ago and images
        response.setReviews(mapReviewsToReviewInfo(product.getReviews()));

        return response;
    }

    private ProductDetailResponse.ShopInfo mapShopToShopInfo(ShopEntity shop) {
        if (shop == null) {
            return null;
        }

        // Hitung median rating untuk semua produk di toko ini
        Double shopMedianRating = ProductUtils.calculateMedianRating(
                shop.getProducts().stream()
                        .flatMap(p -> p.getReviews().stream())
                        .collect(Collectors.toList())
        );

        // Hitung jumlah unique reviewer untuk semua produk di toko ini
        long totalReviewers = ProductUtils.countUniqueReviewers(shop.getProducts());

        return ProductDetailResponse.ShopInfo.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .email(shop.getEmail())
                .shopStatus(shop.getShopStatus())
                .image(shop.getImage())
                .street(shop.getStreet())
                .district(shop.getDistrict())
                .regency(shop.getRegency())
                .province(shop.getProvince())
                .postCode(shop.getPostCode())
                .rating(shopMedianRating)
                .totalReviewedTimes((int) totalReviewers)
                .build();
    }

    private List<ProductDetailResponse.ReviewInfo> mapReviewsToReviewInfo(List<ReviewEntity> reviews) {
        return reviews.stream().map(review -> {
            // Split image string menjadi list (asumsi dipisahkan oleh koma)
            List<String> images = review.getImage() != null ?
                    Arrays.asList(review.getImage().split(",")) :
                    Collections.emptyList();

            return ProductDetailResponse.ReviewInfo.builder()
                    .id(review.getId())
                    .username(review.getCustomer().getUsername())
                    .comment(review.getComment())
                    .images(images)
                    .rating(review.getRating())
                    .timeAgo(ProductUtils.getTimeAgoInIndonesian(review.getCreatedAt()))
                    .build();
        }).collect(Collectors.toList());
    }

    public ResponseEntity<ApiResponse> getProductNearCustomer(String customerId) {
        try {
            log.info("Fetching products near customer: {}", customerId);

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.warn("Customer not found with ID: {}", customerId);
                        return new DataNotFoundException("Customer not found");
                    });

            String customerRegency = customer.getRegency();
            List<ShopEntity> shopsInSameRegency = shopRepository.findByRegency(customerRegency);

            if (shopsInSameRegency.isEmpty()) {
                log.warn("No shops found in regency: {}", customerRegency);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductEntity> products = shopsInSameRegency.stream()
                    .flatMap(shop -> shop.getProducts().stream())
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                log.warn("No products found in shops for regency: {}", customerRegency);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            Collections.shuffle(products);

            List<ProductResponse> responseList = products.stream()
                    .map(this::buildProductResponse)
                    .limit(10)
                    .collect(Collectors.toList());

            log.debug("Found {} products near customer {} in regency {}", responseList.size(), customerId, customerRegency);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching products near customer {}: {}", customerId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductByMostRented() {
        try {
            log.info("Fetching most rented products");

            List<ProductEntity> allProducts = productRepository.findAll();

            if (allProducts.isEmpty()) {
                log.warn("No products found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductResponse> sortedProducts = allProducts.stream()
                    .map(this::buildProductResponse)
                    .sorted(Comparator.comparingInt(ProductResponse::getRentedTimes).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            log.debug("Successfully fetched {} most rented products", sortedProducts.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, sortedProducts);

        } catch (Exception ex) {
            log.error("Error fetching most rented products: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductRecommendedForGuest() {
        try {
            log.info("Fetching recommended products for guest");

            List<ProductEntity> allProducts = productRepository.findAll();

            if (allProducts.isEmpty()) {
                log.warn("No products found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            Collections.shuffle(allProducts);

            List<ProductResponse> responseList = allProducts.stream()
                    .map(this::buildProductResponse)
                    .limit(10)
                    .collect(Collectors.toList());

            log.debug("Successfully fetched {} recommended products for guest", responseList.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (Exception ex) {
            log.error("Error fetching recommended products for guest: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> addProduct(@Valid AddProductRequest request) {
        try {
            log.info("Adding new product for shop ID: {}", request.getShopId());

            ShopEntity shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> {
                        log.warn("Shop not found with ID: {}", request.getShopId());
                        return new DataNotFoundException("Shop not found");
                    });

            ProductEntity newProduct = ProductEntity.builder()
                    .name(request.getName())
                    .category(request.getCategory())
                    .rentCategory(request.getRentCategory())
                    .isRnb(request.isRnb())
                    .weight(request.getWeight())
                    .height(request.getHeight())
                    .width(request.getWidth())
                    .length(request.getLength())
                    .dailyPrice(request.getDailyPrice())
                    .weeklyPrice(request.getWeeklyPrice())
                    .monthlyPrice(request.getMonthlyPrice())
                    .description(request.getDescription())
                    .conditionDescription(request.getConditionDescription())
                    .stock(request.getStock())
                    .minRented(request.getMinRented())
                    .status(request.getStatus())
                    .image(request.getImage())
                    .createdAt(LocalDateTime.now())
                    .lastUpdateAt(LocalDateTime.now())
                    .shop(shop)
                    .build();

            productRepository.save(newProduct);
            log.info("Successfully added new product with ID: {}", newProduct.getId());

            AddProductResponse response = objectMapper.convertValue(newProduct, AddProductResponse.class);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error adding new product: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editProduct(String id, @Valid EditProductRequest request) {
        try {
            log.info("Editing product with ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", id);
                        return new DataNotFoundException("Product not found");
                    });

            ShopEntity shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> {
                        log.warn("Shop not found with ID: {}", request.getShopId());
                        return new DataNotFoundException("Shop not found");
                    });

            // Update product data
            product.setName(request.getName());
            product.setCategory(request.getCategory());
            product.setRentCategory(request.getRentCategory());
            product.setRnb(request.isRnb());
            product.setWeight(request.getWeight());
            product.setHeight(request.getHeight());
            product.setWidth(request.getWidth());
            product.setLength(request.getLength());
            product.setDailyPrice(request.getDailyPrice());
            product.setWeeklyPrice(request.getWeeklyPrice());
            product.setMonthlyPrice(request.getMonthlyPrice());
            product.setDescription(request.getDescription());
            product.setConditionDescription(request.getConditionDescription());
            product.setStock(request.getStock());
            product.setMinRented(request.getMinRented());
            product.setStatus(request.getStatus());
            product.setImage(request.getImage());
            product.setShop(shop);
            product.setLastUpdateAt(LocalDateTime.now());

            productRepository.save(product);
            log.info("Successfully updated product with ID: {}", id);

            EditProductResponse response = EditProductResponse.builder()
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

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error editing product with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> deleteProduct(String id) {
        try {
            log.info("Attempting to delete product with ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", id);
                        return new DataNotFoundException("Product not found");
                    });

            // Check if product has associated transactions
            boolean hasTransactions = !transactionRepository.findByProductId(id).isEmpty();

            if (hasTransactions) {
                log.warn("Cannot delete product with ID: {} - has associated transactions", id);
                return commonUtils.setResponse(
                        ErrorMessageEnum.FAILED,
                        "Product cannot be deleted because it has associated transactions"
                );
            }

            productRepository.delete(product);
            log.info("Successfully deleted product with ID: {}", id);

            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Product deleted successfully");

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error deleting product with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private Double calculateAverageRating(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(ReviewEntity::getRating)
                .average()
                .orElse(0.0);
    }

    private int countRentedTimes(Set<TransactionEntity> transactions) {
        if (transactions == null) {
            return 0;
        }
        return (int) transactions.stream()
                .filter(t -> !t.isSelled())
                .count();
    }

    private ProductResponse buildProductResponse(ProductEntity product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);

        response.setAddress(product.getShop() != null ? product.getShop().getRegency() : "Unknown");

        Double averageRating = calculateAverageRating(product.getReviews());
        response.setRating(averageRating);

        int rentedTimes = countRentedTimes(product.getTransactions());
        response.setRentedTimes(rentedTimes);

        return response;
    }

    private int[] countProductTransactions(String productId) {
        List<TransactionEntity> transactions = transactionRepository.findByProductId(productId);

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

    public ResponseEntity<ApiResponse> getProductByShopId(String shopId) {
        try {
            log.info("Fetching products by shop ID: {}", shopId);

            List<ProductEntity> products = productRepository.findByShopId(shopId);

            if (products.isEmpty()) {
                log.warn("No products found for shop ID: {}", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductResponse> responseList = products.stream()
                    .map(this::buildProductResponse)
                    .collect(Collectors.toList());

            log.debug("Successfully fetched {} products for shop ID: {}", responseList.size(), shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching products by shop ID {}: {}", shopId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }


}