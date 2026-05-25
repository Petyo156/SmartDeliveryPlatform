package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoProductRequest;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Order(5)
@Component
@Slf4j
public class InitializeProducts implements CommandLineRunner {

    private final MerchantService merchantService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public InitializeProducts(MerchantService merchantService,
                              ProductService productService,
                              CategoryService categoryService) {
        this.merchantService = merchantService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        initializeProducts();
    }

    private void initializeProducts() {
        if (productService.productCountMoreThanZero()) {
            return;
        }

        log.info("Initializing demo products...");
        List<DemoProductRequest> demoProducts = getDemoProducts();
        demoProducts.forEach(this::registerProduct);
        log.info("Demo products initialized successfully");
    }

    private void registerProduct(DemoProductRequest request) {
        Merchant merchant = merchantService.getMerchantByUserEmail(request.getMerchantEmail());
        UUID categoryIdByName = categoryService.getGlobalCategoryIdByNameAndType(request.getCategoryName(), merchant.getType());
        ProductRequest productRequest = initializeProductRequest(request, categoryIdByName);
        productService.createProduct(request.getMerchantEmail(), productRequest);
    }

    private ProductRequest initializeProductRequest(DemoProductRequest request, UUID categoryIdByName) {
        return ProductRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .categoryId(categoryIdByName)
                .build();
    }


    private List<DemoProductRequest> getDemoProducts() {
        return List.of(
                // PIZZA HEAVEN (Pizza + Drinks + Desserts)
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Margherita Pizza",
                        "Classic pizza with tomato and mozzarella",
                        BigDecimal.valueOf(12.99),
                        20,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_PIZZA
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Pepperoni Pizza",
                        "Spicy pepperoni pizza",
                        BigDecimal.valueOf(14.99),
                        18,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_PIZZA
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Coca Cola",
                        "Cold drink",
                        BigDecimal.valueOf(2.99),
                        50,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_DRINKS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Chocolate Cake",
                        "Sweet dessert",
                        BigDecimal.valueOf(5.99),
                        15,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_DESSERTS
                ),

                // BURGER KINGDOM (Burgers + Drinks)
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Classic Burger",
                        "Beef burger with vegetables",
                        BigDecimal.valueOf(10.99),
                        25,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_BURGERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Double Cheese Burger",
                        "Double meat and cheese",
                        BigDecimal.valueOf(15.49),
                        20,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_BURGERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Fanta",
                        "Orange soda",
                        BigDecimal.valueOf(2.49),
                        60,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_DRINKS
                ),

                // SUSHI WORLD (Sushi + Salads)
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Salmon Sushi Set",
                        "Fresh salmon sushi",
                        BigDecimal.valueOf(18.99),
                        15,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_SUSHI
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Tuna Roll",
                        "Tuna sushi roll",
                        BigDecimal.valueOf(16.49),
                        12,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_SUSHI
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Green Salad",
                        "Fresh healthy salad",
                        BigDecimal.valueOf(7.99),
                        30,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_SALADS
                ),

                // TECH STORE (Electronics)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Smartphone",
                        "Latest Android phone",
                        BigDecimal.valueOf(699.99),
                        10,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_ELECTRONICS
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Laptop",
                        "Gaming laptop",
                        BigDecimal.valueOf(1299.99),
                        5,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_ELECTRONICS
                ),

                // FRESH MARKET (Groceries)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Milk",
                        "Fresh milk",
                        BigDecimal.valueOf(1.99),
                        100,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_GROCERIES
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Bread",
                        "Fresh bread",
                        BigDecimal.valueOf(1.49),
                        80,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_GROCERIES
                ),

                // FLOWER SHOP (Flowers)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Rose Bouquet",
                        "Beautiful red roses",
                        BigDecimal.valueOf(25.00),
                        20,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_FLOWERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Tulips",
                        "Fresh tulips",
                        BigDecimal.valueOf(18.00),
                        25,
                        "https://via.placeholder.com/300",
                        DemoDataConstants.CATEGORY_FLOWERS
                )
        );
    }
}