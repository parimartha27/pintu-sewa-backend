package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.dto.product.*;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.entity.ProductEntity;
import com.skripsi.siap_sewa.entity.ShopEntity;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.DataNotFoundException;
import com.skripsi.siap_sewa.mapper.ProductMapper;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.repository.ProductRepository;
import com.skripsi.siap_sewa.repository.ShopRepository;
import com.skripsi.siap_sewa.repository.TransactionRepository;
import com.skripsi.siap_sewa.utils.CommonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ProductMapper productMapper;
    private final CommonUtils commonUtils;
    private final ModelMapper modelMapper;

    private static final List<String> VALID_SORT_FIELDS = List.of("name", "dailyPrice", "weeklyPrice", "monthlyPrice", "rating");

    public ResponseEntity<ApiResponse> getProductByMostRented() {
        log.info("Fetching 10 most rented products");
        List<ProductEntity> products = productRepository.findAll();
        if (products.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        List<ProductResponse> response = products.stream()
                .map(productMapper::toProductResponse)
                .sorted(Comparator.comparingInt(ProductResponse::getRentedTimes).reversed())
                .limit(10)
                .toList();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> getProductRecommendedForGuest() {
        log.info("Fetching 10 recommended products for guest");
        List<ProductEntity> products = productRepository.findAll();
        if (products.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        Collections.shuffle(products);
        List<ProductResponse> response = products.stream()
                .limit(10)
                .map(productMapper::toProductResponse)
                .toList();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> getProductNearCustomer(String customerId) {
        log.info("Fetching products near customer: {}", customerId);
        CustomerEntity customer = findCustomerById(customerId);
        String customerRegency = customer.getRegency();

        List<ProductEntity> productsInSameRegency = shopRepository.findByRegency(customerRegency).stream()
                .flatMap(shop -> shop.getProducts().stream())
                .toList();

        if (productsInSameRegency.isEmpty()) {
            log.info("No products found in regency: {}", customerRegency);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        Collections.shuffle(productsInSameRegency);
        List<ProductResponse> response = productsInSameRegency.stream()
                .limit(10)
                .map(productMapper::toProductResponse)
                .toList();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> getProductDetail(String id) {
        log.debug("Fetching product detail for ID: {}", id);
        ProductEntity product = findProductById(id);
        ProductDetailResponse response = productMapper.toProductDetailResponse(product);
        log.info("Successfully built response for product ID: {}", id);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse> addProduct(@Valid AddProductRequest request) {
        log.info("Adding new product for shop ID: {}", request.getShopId());
        ShopEntity shop = findShopById(request.getShopId());

        ProductEntity newProduct = modelMapper.map(request, ProductEntity.class);
        newProduct.setShop(shop);
        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setLastUpdateAt(LocalDateTime.now());

        ProductEntity savedProduct = productRepository.save(newProduct);
        log.info("Successfully added new product with ID: {}", savedProduct.getId());

        AddProductResponse response = modelMapper.map(savedProduct, AddProductResponse.class);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse> editProduct(String id, @Valid EditProductRequest request) {
        log.info("Editing product with ID: {}", id);
        ProductEntity product = findProductById(id);
        ShopEntity shop = findShopById(request.getShopId());

       
        modelMapper.map(request, product);
        product.setShop(shop);
        product.setLastUpdateAt(LocalDateTime.now());

        ProductEntity updatedProduct = productRepository.save(product);
        log.info("Successfully updated product with ID: {}", id);

        EditProductResponse response = productMapper.toEditProductResponse(updatedProduct);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse> deleteProduct(String id) {
        log.info("Attempting to delete product with ID: {}", id);
        ProductEntity product = findProductById(id);

        if (!transactionRepository.findByProductId(id).isEmpty()) {
            log.warn("Cannot delete product ID: {} - it has associated transactions.", id);
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Product cannot be deleted, it has transactions.");
        }

        productRepository.delete(product);
        log.info("Successfully deleted product with ID: {}", id);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Product deleted successfully");
    }

    public ResponseEntity<ApiResponse> getProductByShopId(String shopId) {
        log.info("Fetching up to 5 products by shop ID: {}", shopId);
        List<ProductEntity> products = productRepository.findByShopId(shopId);
        if (products.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        List<ProductResponse> response = products.stream()
                .limit(5)
                .map(productMapper::toProductResponse)
                .toList();
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    public ResponseEntity<ApiResponse> getTopProductByShopId(String shopId) {
        log.info("Fetching top 10 products from shopId: {}", shopId);
        findShopById(shopId);
        List<ProductEntity> products = productRepository.findByShopId(shopId);
        if (products.isEmpty()) {
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        Collections.shuffle(products);
        List<ProductResponse> response = products.stream()
                .limit(10)
                .map(productMapper::toProductResponse)
                .toList();

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse> editStockProduct(String id, Integer newStock) {
        log.info("Editing stock for product ID: {}", id);
        ProductEntity product = findProductById(id);

        int stockToSet = (newStock != null && newStock > 0) ? newStock : 0;
        product.setStock(stockToSet);
        product.setLastUpdateAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Successfully updated stock for product ID {} to {}", id, stockToSet);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, null);
    }

    public ResponseEntity<ApiResponse> getProductsByShop(String shopId, ProductFilterDto filters) {
        log.debug("Fetching products for shop ID: {} with filters: {}", shopId, filters);
        findShopById(shopId);

        Pageable pageable = createPageable(filters);
        Specification<ProductEntity> spec = createSpecification(shopId, filters);

        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        List<ProductListResponse> productResponses = productsPage.getContent().stream()
                .map(productMapper::toProductListResponse)
                .toList();

        Map<String, Object> responseData = Map.of(
                "products", productResponses,
                "current_page", productsPage.getNumber(),
                "total_items", productsPage.getTotalElements(),
                "total_pages", productsPage.getTotalPages()
        );

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, responseData);
    }

    private ProductEntity findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found with ID: " + id));
    }

    private ShopEntity findShopById(String shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new DataNotFoundException("Shop not found with ID: " + shopId));
    }

    private CustomerEntity findCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new DataNotFoundException("Customer not found with ID: " + customerId));
    }

    private Pageable createPageable(ProductFilterDto filters) {
        String sortBy = (filters.getSortBy() != null && VALID_SORT_FIELDS.contains(filters.getSortBy()))
                ? filters.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filters.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(filters.getPage(), 16, Sort.by(direction, sortBy));
    }

    private Specification<ProductEntity> createSpecification(String shopId, ProductFilterDto filters) {
        Specification<ProductEntity> spec = (root, query, cb) -> cb.equal(root.get("shop").get("id"), shopId);

        if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), filters.getCategory()));
        }
        if (filters.getIsRnb() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isRnb"), filters.getIsRnb()));
        }
        if (filters.getSearch() != null && !filters.getSearch().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + filters.getSearch().toLowerCase() + "%"));
        }
        return spec;
    }
}