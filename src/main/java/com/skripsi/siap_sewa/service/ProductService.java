package com.skripsi.siap_sewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.siap_sewa.dto.product.ProductDetailResponse;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.entity.*;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.repository.*;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.ProductUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public ResponseEntity<ApiResponse> getProductByMostRented() {
        try {
            log.info("Fetching most rented products");

            List<ProductEntity> allProducts = productRepository.findAll();

            if (allProducts.isEmpty()) {
                log.info("No products found in database");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductResponse> sortedProducts = allProducts.stream()
                    .map(this::buildProductResponse)
                    .sorted(Comparator.comparingInt(ProductResponse::getRentedTimes).reversed())
                    .limit(10)
                    .toList();

            log.info("Successfully fetched {} most rented products", sortedProducts.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, sortedProducts);

        } catch (Exception ex) {
            log.info("Error fetching most rented products: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }
    
    public ResponseEntity<ApiResponse> getProductRecommendedForGuest() {
        try {
            log.info("Fetching recommended products for guest");

            List<ProductEntity> allProducts = productRepository.findAll();

            if (allProducts.isEmpty()) {
                log.info("No products found in database for guest");
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            Collections.shuffle(allProducts);

            List<ProductResponse> responseList = allProducts.stream()
                    .map(this::buildProductResponse)
                    .limit(10)
                    .toList();

            log.info("Successfully fetched {} recommended products for guest", responseList.size());
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (Exception ex) {
            log.info("Error fetching recommended products for guest: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductNearCustomer(String customerId) {
        try {
            log.info("Fetching products near customer: {}", customerId);

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.info("Customer not found with ID: {}", customerId);
                        return new DataNotFoundException("Customer not found");
                    });

            String customerRegency = customer.getRegency();
            List<ShopEntity> shopsInSameRegency = shopRepository.findByRegency(customerRegency);

            if (shopsInSameRegency.isEmpty()) {
                log.info("No shops found in regency: {}", customerRegency);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductEntity> products = shopsInSameRegency.stream()
                    .flatMap(shop -> shop.getProducts().stream())
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                log.info("No products found in shops for regency: {}", customerRegency);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            Collections.shuffle(products);

            List<ProductResponse> responseList = products.stream()
                    .map(this::buildProductResponse)
                    .limit(10)
                    .toList();

            log.info("Found {} products near customer {} in regency {}", responseList.size(), customerId, customerRegency);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error fetching products near customer {}: {}", customerId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductDetail(String id) {
        try {
            log.info("Fetching product details for ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Product not found with ID: {} in getProductDetail", id);
                        return new DataNotFoundException("Product not found in getProductDetail");
                    });

            ProductDetailResponse response = modelMapper.map(product, ProductDetailResponse.class);
            response.setRating(ProductUtils.calculateWeightedRating(product.getReviews()));

            log.info("Successfully fetched product details for ID: {}", id);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error fetching product details for ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> addProduct(@Valid AddProductRequest request) {
        try {
            log.info("Adding new product for shop ID: {}", request.getShopId());

            ShopEntity shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", request.getShopId());
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
            log.info("Error adding new product: {}", ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> editProduct(String id, @Valid EditProductRequest request) {
        try {
            log.info("Editing product with ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Product not found with ID: {}", id);
                        return new DataNotFoundException("Product not found");
                    });

            ShopEntity shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> {
                        log.info("Shop not found with ID: {}", request.getShopId());
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
            log.info("Error editing product with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> deleteProduct(String id) {
        try {
            log.info("Attempting to delete product with ID: {}", id);

            ProductEntity product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.info("Product not found with ID: {}", id);
                        return new DataNotFoundException("Product not found");
                    });

            // Check if product has associated transactions
            boolean hasTransactions = !transactionRepository.findByProductId(id).isEmpty();

            if (hasTransactions) {
                log.info("Cannot delete product with ID: {} - has associated transactions", id);
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
            log.info("Error deleting product with ID {}: {}", id, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<ApiResponse> getProductByShopId(String shopId) {
        try {
            log.info("Fetching products by shop ID: {}", shopId);

            List<ProductEntity> products = productRepository.findByShopId(shopId);

            if (products.isEmpty()) {
                log.info("No products found for shop ID: {}", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
            }

            List<ProductResponse> responseList = products.stream()
                    .map(this::buildProductResponse)
                    .toList();

            log.info("Successfully fetched {} products for shop ID: {}", responseList.size(), shopId);
            return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseList);

        } catch (DataNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info("Error fetching products by shop ID {}: {}", shopId, ex.getMessage(), ex);
            return commonUtils.setResponse(ErrorMessageEnum.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ProductResponse buildProductResponse(ProductEntity product) {
        return getProductResponse(product);
    }

    static ProductResponse getProductResponse(ProductEntity product) {
        Double productRating = ProductUtils.calculateWeightedRating(product.getReviews());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .rentCategory(CommonUtils.getRentDurationName(product.getRentCategory()))
                .isRnb(product.isRnb())
                .image(product.getImage())
                .address(product.getShop() != null ? product.getShop().getRegency() : "Kabupaten")
                .rating(productRating)
                .rentedTimes(ProductUtils.countRentedTimes(product.getTransactions()))
                .price(ProductUtils.getLowestPrice(product))
                .build();
    }


}