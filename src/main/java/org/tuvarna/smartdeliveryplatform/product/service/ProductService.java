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
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductCategorySectionResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public void createProduct(String merchantEmail, ProductRequest request) {
        Merchant merchant = merchantService.getMerchantByUserEmail(merchantEmail);
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

    @Transactional
    public List<ProductResponse> getAvailableProductsForMerchantSlug(String merchantSlug) {
        return productRepository.findAllByMerchant_SlugAndIsAvailableTrueAndIsDeletedFalseOrderByCategory_NameAscCreatedAtDesc(merchantSlug)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional
    public List<ProductCategorySectionResponse> getAvailableProductSectionsForMerchantSlug(String merchantSlug) {
        List<ProductResponse> products = getAvailableProductsForMerchantSlug(merchantSlug);
        return groupProductsByCategory(products);
    }

    @Transactional
    public List<ProductCategorySectionResponse> getAvailableProductSectionsForMerchantSlug(String merchantSlug, String category) {
        List<ProductResponse> products = getAvailableProductsForMerchantSlug(merchantSlug);
        if (category == null || category.isBlank()) {
            return groupProductsByCategory(products);
        }

        List<ProductResponse> filteredProducts = products.stream()
                .filter(product -> product.getCategoryName().equals(category))
                .toList();

        return groupProductsByCategory(filteredProducts);
    }

    @Transactional
    public List<String> getAvailableCategoryNamesForMerchantSlug(String merchantSlug) {
        return getAvailableProductSectionsForMerchantSlug(merchantSlug)
                .stream()
                .map(ProductCategorySectionResponse::getCategoryName)
                .toList();
    }

    public void deleteProduct(String slug, AuthenticationMetadata authenticationMetadata) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());
        Product product = getProductBySlugAndMerchant(slug, merchant);
        product.setIsDeleted(true);
        productRepository.save(product);
        log.info("Soft deleted product: {}", slug);
    }

    public boolean productCountMoreThanZero() {
        return productRepository.count() > 0;
    }

    public void toggleAvailability(String slug, AuthenticationMetadata authenticationMetadata) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());
        Product product = getProductBySlugAndMerchant(slug, merchant);
        product.setIsAvailable(!product.getIsAvailable());
        productRepository.save(product);
        log.info("Toggled availability for product: {} to {}", slug, product.getIsAvailable());
    }

    private List<ProductCategorySectionResponse> groupProductsByCategory(List<ProductResponse> products) {
        List<ProductCategorySectionResponse> productSections = new ArrayList<>();

        for (ProductResponse product : products) {
            if (productSections.isEmpty() || !lastSectionHasCategory(productSections, product.getCategoryName())) {
                productSections.add(ProductCategorySectionResponse.builder()
                        .categoryName(product.getCategoryName())
                        .products(new ArrayList<>())
                        .build());
            }

            productSections.getLast().getProducts().add(product);
        }

        return productSections;
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

    private boolean lastSectionHasCategory(List<ProductCategorySectionResponse> sections, String categoryName) {
        ProductCategorySectionResponse lastSection = sections.getLast();
        return lastSection.getCategoryName().equals(categoryName);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .slug(product.getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
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
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .slug(slug)
                .build();
    }
}
