package org.tuvarna.smartdeliveryplatform.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.category.repository.CategoryRepository;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryPillResponse;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MerchantService merchantService;
    private final ProductCategoryService productCategoryService;

    public CategoryService(CategoryRepository categoryRepository, MerchantService merchantService, ProductCategoryService productCategoryService) {
        this.categoryRepository = categoryRepository;
        this.merchantService = merchantService;
        this.productCategoryService = productCategoryService;
    }

    public List<CategoryResponse> getGlobalAvailableCategories(String email) {
        Merchant merchantByUserEmail = merchantService.getMerchantByUserEmail(email);
        return toCategoryResponses(getGlobalCategoriesByType(merchantByUserEmail.getType()));
    }

    public List<CategoryResponse> getMerchantAvailableCategories(String email) {
        Merchant merchantByUserEmail = merchantService.getMerchantByUserEmail(email);
        return toCategoryResponses(getMerchantCategories(merchantByUserEmail));
    }

    @Transactional
    public void createMerchantCategory(AuthenticationMetadata authenticationMetadata, String categoryName) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());

        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        if (categoryRepository.existsByNameIgnoreCaseAndMerchantAndIsDeletedFalse(categoryName, merchant)) {
            throw new IllegalStateException("Category with this name already exists for this merchant");
        }

        Category category = initializeCategory(merchant, categoryName);
        categoryRepository.save(category);
        log.info("Created merchant category: {} for merchant: {}", categoryName, merchant.getName());
    }

    @Transactional
    public void deleteMerchantCategory(AuthenticationMetadata authenticationMetadata, String categoryId) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());

        UUID categoryUUID = UUID.fromString(categoryId);
        Category category = getCategoryById(categoryUUID);

        if (category.getIsGlobal()) {
            throw new IllegalStateException("Global categories cannot be deleted");
        }

        if (!category.getMerchant().getId().equals(merchant.getId())) {
            throw new IllegalStateException("You cannot delete another merchant's category");
        }

        boolean hasProducts = productCategoryService.categoryHasProducts(category);

        if (hasProducts) {
            throw new IllegalStateException("Category contains products");
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);

        log.info("Soft deleted category {} for merchant {}", category.getName(), merchant.getName());
    }

    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category with this id does not exist"));
    }

    public UUID getGlobalCategoryIdByNameAndType(String categoryName, MerchantType merchantType) {
        return categoryRepository.findCategoryByNameAndTypeAndIsGlobalTrue(categoryName, merchantType)
                .orElseThrow(() -> new IllegalStateException("Global category for this merchant type with this name does not exist"))
                .getId();
    }

    public List<CategoryPillResponse> getGlobalRestaurantCategories() {
        List<Category> categories = categoryRepository.findAllByIsGlobalTrueAndTypeAndIsDeletedFalse(MerchantType.RESTAURANT);
        return categories.stream()
                .map(category -> CategoryPillResponse.builder()
                        .name(category.getName())
                        .build())
                .toList();
    }

    public List<CategoryPillResponse> getGlobalShopCategories() {
        List<Category> categories = categoryRepository.findAllByIsGlobalTrueAndTypeAndIsDeletedFalse(MerchantType.SHOP);
        return categories.stream()
                .map(category -> CategoryPillResponse.builder()
                        .name(category.getName())
                        .build())
                .toList();
    }

    public boolean existsByNameAndType(String name, MerchantType type) {
        return categoryRepository.existsByNameAndType(name, type);
    }

    public void saveGlobalCategory(Category globalCategory) {
        categoryRepository.save(globalCategory);
    }

    public boolean categoriesCountMoreThanZero() {
        return categoryRepository.count() > 0;
    }

    private List<Category> getGlobalCategoriesByType(MerchantType type) {
        return categoryRepository.findAllByIsGlobalTrueAndTypeAndIsDeletedFalse(type);
    }

    private List<Category> getMerchantCategories(Merchant merchant) {
        return categoryRepository.findAllByMerchantAndIsDeletedFalse(merchant);
    }

    private List<CategoryResponse> toCategoryResponses(List<Category> categories) {
        return categories.stream()
                .map(this::initializeCategoryResponse)
                .toList();
    }

    private Category initializeCategory(Merchant merchant, String name) {
        return Category.builder()
                .name(name)
                .merchant(merchant)
                .type(merchant.getType())
                .isGlobal(false)
                .isDeleted(false)
                .build();
    }

    private CategoryResponse initializeCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .isGlobal(category.getIsGlobal())
                .build();
    }
}