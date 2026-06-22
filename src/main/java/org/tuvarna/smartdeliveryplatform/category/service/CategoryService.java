package org.tuvarna.smartdeliveryplatform.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.category.repository.CategoryRepository;
import org.tuvarna.smartdeliveryplatform.exception.CategoryOperationException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
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
            throw new CategoryOperationException(ExceptionMessages.CATEGORY_NAME_REQUIRED);
        }

        if (categoryRepository.existsByNameIgnoreCaseAndMerchantAndIsDeletedFalse(categoryName, merchant)) {
            throw new CategoryOperationException(ExceptionMessages.CATEGORY_ALREADY_EXISTS_FOR_MERCHANT);
        }

        Category category = initializeCategory(merchant, categoryName);
        categoryRepository.save(category);
        log.info("Created merchant category: {} for merchant: {}", categoryName, merchant.getName());
    }

    @Transactional
    public void deleteMerchantCategory(AuthenticationMetadata authenticationMetadata, UUID categoryId) {
        Merchant merchant = merchantService.getMerchantByUserEmail(authenticationMetadata.getUsername());

        Category category = getCategoryById(categoryId);

        if (category.getIsGlobal()) {
            throw new CategoryOperationException(ExceptionMessages.GLOBAL_CATEGORIES_CANNOT_BE_DELETED);
        }

        if (!category.getMerchant().getId().equals(merchant.getId())) {
            throw new CategoryOperationException(ExceptionMessages.CANNOT_DELETE_ANOTHER_MERCHANT_CATEGORY);
        }

        boolean hasProducts = productCategoryService.categoryHasProducts(category);

        if (hasProducts) {
            throw new CategoryOperationException(ExceptionMessages.CATEGORY_CONTAINS_PRODUCTS);
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);

        log.info("Soft deleted category {} for merchant {}", category.getName(), merchant.getName());
    }

    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryOperationException(ExceptionMessages.CATEGORY_WITH_ID_DOES_NOT_EXIST));
    }

    public UUID getGlobalCategoryIdByNameAndType(String categoryName, MerchantType merchantType) {
        return categoryRepository.findCategoryByNameAndTypeAndIsGlobalTrue(categoryName, merchantType)
                .orElseThrow(() -> new SystemOperationException(ExceptionMessages.GLOBAL_CATEGORY_NOT_FOUND_FOR_MERCHANT_TYPE))
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
