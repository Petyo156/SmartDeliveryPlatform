package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.Arrays;
import java.util.List;

@Order(3)
@Component
@Slf4j
public class InitializeCategories implements CommandLineRunner {

    private final CategoryService categoryService;

    public InitializeCategories(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        initializeGlobalCategories();
    }

    private void initializeGlobalCategories() {
        if (categoryService.categoriesCountMoreThanZero()) {
            return;
        }

        List<String> restaurantCategories = initializeDemoRestaurantCategories();
        List<String> shopCategories = initializeDemoShopCategories();

        initializeCategoriesForType(restaurantCategories, MerchantType.RESTAURANT);
        initializeCategoriesForType(shopCategories, MerchantType.SHOP);
        log.info("Global categories initialized successfully");
    }

    private void initializeCategoriesForType(List<String> categoryNames, MerchantType type) {
        for (String name : categoryNames) {
            if (!categoryService.existsByNameAndType(name, type)) {
                Category globalCategory = initializeGlobalCategory(type, name);
                categoryService.saveGlobalCategory(globalCategory);
                log.info("Created global category: {} for type: {}", name, type);
            }
        }
    }

    private Category initializeGlobalCategory(MerchantType type, String name) {
        return Category.builder()
                .name(name)
                .type(type)
                .isGlobal(true)
                .merchant(null)
                .isDeleted(false)
                .build();
    }

    private List<String> initializeDemoShopCategories() {
        return Arrays.asList(
                DemoDataConstants.CATEGORY_ELECTRONICS,
                DemoDataConstants.CATEGORY_GROCERIES,
                DemoDataConstants.CATEGORY_FLOWERS,
                DemoDataConstants.CATEGORY_PHARMACY,
                DemoDataConstants.CATEGORY_PET_SUPPLIES
        );
    }

    private List<String> initializeDemoRestaurantCategories() {
        return Arrays.asList(
                DemoDataConstants.CATEGORY_PIZZA,
                DemoDataConstants.CATEGORY_BURGERS,
                DemoDataConstants.CATEGORY_SUSHI,
                DemoDataConstants.CATEGORY_DRINKS,
                DemoDataConstants.CATEGORY_DESSERTS,
                DemoDataConstants.CATEGORY_SALADS
        );
    }
}
