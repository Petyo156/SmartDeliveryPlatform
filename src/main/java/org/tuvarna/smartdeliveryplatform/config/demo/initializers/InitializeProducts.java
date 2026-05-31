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
                        "https://content.jwplatform.com/v2/media/Bt9tKjiM/poster.jpg?width=720",
                        DemoDataConstants.CATEGORY_PIZZA
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Pepperoni Pizza",
                        "Spicy pepperoni pizza",
                        BigDecimal.valueOf(14.99),
                        "https://www.hunts.com/sites/g/files/qyyrlu211/files/uploadedImages/img_6934_48664.jpg",
                        DemoDataConstants.CATEGORY_PIZZA
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Coca Cola",
                        "Cold drink",
                        BigDecimal.valueOf(2.99),
                        "https://miamikosherfood.com/cdn/shop/files/049000050103.jpg?v=1765824810",
                        DemoDataConstants.CATEGORY_DRINKS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Chocolate Cake",
                        "Sweet dessert",
                        BigDecimal.valueOf(5.99),
                        "https://sallysbakingaddiction.com/wp-content/uploads/2013/04/triple-chocolate-cake-4-600x900.jpg",
                        DemoDataConstants.CATEGORY_DESSERTS
                ),

                // BURGER KINGDOM (Burgers + Drinks)
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Classic Burger",
                        "Beef burger with vegetables",
                        BigDecimal.valueOf(10.99),
                        "https://homefoodie.com.ph/uploads/2024/CLASSIC%20BURGER.jpg",
                        DemoDataConstants.CATEGORY_BURGERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Double Cheese Burger",
                        "Double meat and cheese",
                        BigDecimal.valueOf(15.49),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRPl9lncUDCC6xQ0BJoi7pJDa_tDZLtxivy4w&s",
                        DemoDataConstants.CATEGORY_BURGERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Fanta",
                        "Orange soda",
                        BigDecimal.valueOf(2.49),
                        "https://m.ebag.bg/en/products/4156/images/2/400",
                        DemoDataConstants.CATEGORY_DRINKS
                ),

                // SUSHI WORLD (Sushi + Salads)
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Salmon Sushi Set",
                        "Fresh salmon sushi",
                        BigDecimal.valueOf(18.99),
                        "https://kayo.bg/sites/default/files/siomga_set.jpg",
                        DemoDataConstants.CATEGORY_SUSHI
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Tuna Roll",
                        "Tuna sushi roll",
                        BigDecimal.valueOf(16.49),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQbfV7iV6CZBb5VR_92P5pFHdcbtsNv8ABAKg&s",
                        DemoDataConstants.CATEGORY_SUSHI
                ),
                new DemoProductRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Green Salad",
                        "Fresh healthy salad",
                        BigDecimal.valueOf(7.99),
                        "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chopped-green-salad-with-herby-chilli-dressing-429ab82.jpg?quality=90&resize=500,454",
                        DemoDataConstants.CATEGORY_SALADS
                ),

                // TECH STORE (Electronics)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Smartphone",
                        "Latest Android phone",
                        BigDecimal.valueOf(699.99),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9M4ydwp26-7mEzPMlRvh_j-p3gWwNV7vx9Q&s",
                        DemoDataConstants.CATEGORY_ELECTRONICS
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Laptop",
                        "Gaming laptop",
                        BigDecimal.valueOf(1299.99),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSZWcgGtAEhOZRRofkCAdeOK3LPF7UCz_ZKxA&s",
                        DemoDataConstants.CATEGORY_ELECTRONICS
                ),

                // FRESH MARKET (Groceries)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Milk",
                        "Fresh milk",
                        BigDecimal.valueOf(1.99),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRdt7a0w7WPbJ25VQXpcAEO-Z5drxudGaRXpg&s",
                        DemoDataConstants.CATEGORY_GROCERIES
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Bread",
                        "Fresh bread",
                        BigDecimal.valueOf(1.49),
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRfvc5-n0439EwjQWopFuSfqK8y3Xv_BHSZYA&s",
                        DemoDataConstants.CATEGORY_GROCERIES
                ),

                // FLOWER SHOP (Flowers)
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Rose Bouquet",
                        "Beautiful red roses",
                        BigDecimal.valueOf(25.00),
                        "https://www.flowersforeveryone.com.au/cdn/shop/files/104376_FFE_Sh06_182.jpg?v=1769029217&width=1445",
                        DemoDataConstants.CATEGORY_FLOWERS
                ),
                new DemoProductRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Tulips",
                        "Fresh tulips",
                        BigDecimal.valueOf(18.00),
                        "https://onlineflowerexpress.com/cdn/shop/files/PinkTulipsLove.webp?v=1691571050",
                        DemoDataConstants.CATEGORY_FLOWERS
                )
        );
    }
}
