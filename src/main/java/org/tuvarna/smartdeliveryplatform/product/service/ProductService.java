package org.tuvarna.smartdeliveryplatform.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.model.Product;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.utils.SlugUtil;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final MerchantService merchantService;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, MerchantService merchantService, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.merchantService = merchantService;
        this.categoryService = categoryService;
    }

    @Transactional
    public void createProduct(AuthenticationMetadata authenticationMetadata, ProductRequest request) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());
        Category category = categoryService.getCategoryById(request.getCategoryId());

        boolean isGlobal = category.getIsGlobal();
        boolean isMerchantOwnCategory = category.getMerchant() != null && category.getMerchant().getId().equals(merchant.getId());

        if (!isGlobal && !isMerchantOwnCategory) {
            throw new IllegalStateException("Merchant can only create products in global categories or their own categories");
        }

        Product product = initializeProduct(request, merchant, category);
        productRepository.save(product);
        log.info("Created product: {} for merchant: {}", request.getName(), merchant.getName());
    }

    @Transactional
    public List<ProductResponse> getMerchantProductResponses(String email) {
        Merchant merchant = merchantService.getMerchantByUserEmail(email);
        return getMerchantProducts(merchant).stream()
                .map(this::toProductResponse)
                .toList();
    }

    public void deleteProduct(String slug, AuthenticationMetadata authenticationMetadata) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());
        Product product = getProductBySlugAndMerchant(slug, merchant);
        product.setIsDeleted(true);
        productRepository.save(product);
        log.info("Soft deleted product: {}", slug);
    }

    public void toggleAvailability(String slug, AuthenticationMetadata authenticationMetadata) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());
        Product product = getProductBySlugAndMerchant(slug, merchant);
        product.setIsAvailable(!product.getIsAvailable());
        productRepository.save(product);
        log.info("Toggled availability for product: {} to {}", slug, product.getIsAvailable());
    }

    private Product getProductBySlugAndMerchant(String slug, Merchant merchant) {
        Optional<Product> product = productRepository.findBySlugAndMerchant(slug, merchant);
        if(product.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        return product.get();
    }

    private List<Product> getMerchantProducts(Merchant merchant) {
        return productRepository.findAllByMerchantAndIsDeletedFalse(merchant);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .isAvailable(product.getIsAvailable())
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory().getName())
                .build();
    }

    private String initializeProductSlug(Merchant merchant, ProductRequest product) {
        String merchantSlug = merchant.getSlug();
        String productSlug = SlugUtil.normalize(product.getName());
        return merchantSlug + "-" + productSlug + "-" + SlugUtil.randomSuffix();
    }

    private Product initializeProduct(ProductRequest request, Merchant merchant, Category category) {
        String slug = initializeProductSlug(merchant, request);
        return Product.builder()
                .merchant(merchant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .slug(slug)
                .build();
    }
}