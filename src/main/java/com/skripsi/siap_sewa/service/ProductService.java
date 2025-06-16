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
import org.springframework.web.multipart.MultipartFile;

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
    private final CloudinaryService cloudinaryService;

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

        
        validateImageFile(request.getImage());

        ShopEntity shop = findShopById(request.getShopId());

        ProductEntity newProduct = modelMapper.map(request, ProductEntity.class);
        newProduct.setShop(shop);
        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setLastUpdateAt(LocalDateTime.now());

        
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(
                        request.getImage()
                );
                newProduct.setImage(imageUrl);
                log.info("Image uploaded successfully for new product");
            } catch (Exception e) {
                log.error("Failed to upload image for new product", e);
                throw new RuntimeException("Gagal upload gambar produk: " + e.getMessage());
            }
        }

        ProductEntity savedProduct = productRepository.save(newProduct);
        log.info("Successfully added new product with ID: {}", savedProduct.getId());

        AddProductResponse response = modelMapper.map(savedProduct, AddProductResponse.class);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    @Transactional
    public ResponseEntity<ApiResponse> editProduct(String id, @Valid EditProductRequest request) {
        log.info("Editing product with ID: {}", id);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            request.getImages().forEach(this::validateImageFile);
        }

        ProductEntity product = findProductById(id);
        ShopEntity shop = findShopById(request.getShopId());

        mapEditRequestToProductEntity(request, product);
        product.setShop(shop);
        product.setLastUpdateAt(LocalDateTime.now());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                String oldImageString = product.getImage();
                List<String> oldImageUrls = (oldImageString != null && !oldImageString.isEmpty())
                        ? Arrays.asList(oldImageString.split(";"))
                        : Collections.emptyList();

                List<String> newImageUrls = new ArrayList<>();
                for (MultipartFile imageFile : request.getImages()) {
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(imageFile);
                        newImageUrls.add(imageUrl);
                    }
                }

                if (!newImageUrls.isEmpty()) {
                    String concatenatedImageUrls = String.join(";", newImageUrls);
                    product.setImage(concatenatedImageUrls);
                    log.info("New image URLs concatenated for product ID: {}", id);

                    if (!oldImageUrls.isEmpty()) {
                        oldImageUrls.forEach(oldUrl -> {
                            try {
                                cloudinaryService.deleteImage(oldUrl);
                            } catch (Exception e) {
                                log.error("Failed to delete old image: {}", oldUrl, e);
                            }
                        });
                        log.info("Successfully deleted {} old images from Cloudinary for product ID: {}", oldImageUrls.size(), id);
                    }
                }

            } catch (Exception e) {
                log.error("Failed to upload new images for product ID: {}", id, e);
                throw new RuntimeException("Gagal upload gambar produk: " + e.getMessage());
            }
        }

        ProductEntity updatedProduct = productRepository.save(product);
        log.info("Successfully updated product with ID: {}", id);

        EditProductResponse response = productMapper.toEditProductResponse(updatedProduct);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return; 
        }

        
        long maxSize = 5 * 1024 * 1024; 
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("Ukuran file gambar tidak boleh lebih dari 5MB");
        }

        
        String contentType = image.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Format file harus berupa gambar (JPEG, PNG, WebP)");
        }

        
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            throw new IllegalArgumentException("File harus berupa gambar dengan ekstensi yang valid");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/jpg");
    }

    private boolean hasValidImageExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") ||
                lowerCaseFilename.endsWith(".jpeg") ||
                lowerCaseFilename.endsWith(".png") ||
                lowerCaseFilename.endsWith(".webp");
    }

    @Transactional
    public ResponseEntity<ApiResponse> deleteProduct(String id) {
        log.info("Attempting to delete product with ID: {}", id);

        ProductEntity product = findProductById(id);

        
        if (!transactionRepository.findByProductId(id).isEmpty()) {
            log.warn("Cannot delete product ID: {} - it has associated transactions.", id);
            return commonUtils.setResponse(ErrorMessageEnum.FAILED, "Product cannot be deleted, it has transactions.");
        }

        
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                cloudinaryService.deleteImage(product.getImage());
                log.info("Image deleted from Cloudinary for product ID: {}", id);
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary for product ID: {}, but continuing with product deletion", id, e);
                
            }
        }

        productRepository.delete(product);
        log.info("Successfully deleted product with ID: {}", id);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, "Product deleted successfully");
    }

    
    public ResponseEntity<ApiResponse> getProductByShopId(String shopId) {
        log.info("Fetching up to 5 products by shop ID: {}", shopId);

        List<ProductEntity> products = productRepository.findByShopId(shopId);
        if (products.isEmpty()) {
            log.info("No products found for shop ID: {}", shopId);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, null);
        }

        List<ProductResponse> response = products.stream()
                .limit(5)
                .map(product -> {
                    ProductResponse productResponse = productMapper.toProductResponse(product);
                    
                    if (product.getImage() != null) {
                        productResponse.setImage(product.getImage());
                    }
                    return productResponse;
                })
                .toList();

        log.info("Successfully fetched {} products for shop ID: {}", response.size(), shopId);
        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS, response);
    }

    
    @Transactional
    public ResponseEntity<ApiResponse> deleteProductsByShopId(String shopId) {
        log.info("Attempting to delete all products for shop ID: {}", shopId);

        List<ProductEntity> products = productRepository.findByShopId(shopId);
        if (products.isEmpty()) {
            log.info("No products found for shop ID: {}", shopId);
            return commonUtils.setResponse(ErrorMessageEnum.DATA_NOT_FOUND, "No products found for this shop");
        }

        
        List<String> productIds = products.stream().map(ProductEntity::getId).toList();
        for (String productId : productIds) {
            if (!transactionRepository.findByProductId(productId).isEmpty()) {
                log.warn("Cannot delete products for shop ID: {} - some products have associated transactions", shopId);
                return commonUtils.setResponse(ErrorMessageEnum.FAILED,
                        "Cannot delete products, some products have associated transactions.");
            }
        }

        
        int deletedImagesCount = 0;
        for (ProductEntity product : products) {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(product.getImage());
                    deletedImagesCount++;
                } catch (Exception e) {
                    log.error("Failed to delete image from Cloudinary for product ID: {}", product.getId(), e);
                    
                }
            }
        }

        
        productRepository.deleteAll(products);

        log.info("Successfully deleted {} products and {} images for shop ID: {}",
                products.size(), deletedImagesCount, shopId);

        return commonUtils.setResponse(ErrorMessageEnum.SUCCESS,
                String.format("Successfully deleted %d products", products.size()));
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

    
    private void mapEditRequestToProductEntity(EditProductRequest request, ProductEntity product) {
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        if (request.getRentCategory() != null) {
            product.setRentCategory(request.getRentCategory());
        }

        product.setRnb(request.isRnb());

        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
        }

        if (request.getHeight() != null) {
            product.setHeight(request.getHeight());
        }

        if (request.getWidth() != null) {
            product.setWidth(request.getWidth());
        }

        if (request.getLength() != null) {
            product.setLength(request.getLength());
        }

        if (request.getDailyPrice() != null) {
            product.setDailyPrice(request.getDailyPrice());
        }

        if (request.getWeeklyPrice() != null) {
            product.setWeeklyPrice(request.getWeeklyPrice());
        }

        if (request.getMonthlyPrice() != null) {
            product.setMonthlyPrice(request.getMonthlyPrice());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getConditionDescription() != null) {
            product.setConditionDescription(request.getConditionDescription());
        }

        product.setStock(request.getStock());
        product.setMinRented(request.getMinRented());

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
    }
}