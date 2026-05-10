package org.tuvarna.smartdeliveryplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.category.repository.CategoryRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.Arrays;
import java.util.List;

@Order(2)
@Component
@Slf4j
public class InitializeCategories implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public InitializeCategories(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        initializeGlobalCategories();
    }

    private void initializeGlobalCategories() {
        List<String> restaurantCategories = Arrays.asList(
                "Pizza",
                "Burgers",
                "Sushi",
                "Drinks",
                "Desserts",
                "Salads"
        );

        List<String> shopCategories = Arrays.asList(
                "Electronics",
                "Groceries",
                "Flowers",
                "Pharmacy",
                "Pet Supplies"
        );

        initializeCategoriesForType(restaurantCategories, MerchantType.RESTAURANT);
        initializeCategoriesForType(shopCategories, MerchantType.SHOP);

        log.info("Global categories initialized successfully");
    }

    private void initializeCategoriesForType(List<String> categoryNames, MerchantType type) {
        for (String name : categoryNames) {
            if (!categoryRepository.existsByNameAndType(name, type)) {
                Category category = Category.builder()
                        .name(name)
                        .type(type)
                        .isGlobal(true)
                        .merchant(null)
                        .isDeleted(false)
                        .build();
                categoryRepository.save(category);
                log.info("Created global category: {} for type: {}", name, type);
            }
        }
    }
}
