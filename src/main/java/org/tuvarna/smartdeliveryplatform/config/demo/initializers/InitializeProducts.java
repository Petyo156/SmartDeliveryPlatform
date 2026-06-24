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
                product(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Margherita Pizza", "Classic pizza with tomato and mozzarella", 12.99, "https://content.jwplatform.com/v2/media/Bt9tKjiM/poster.jpg?width=720", DemoDataConstants.CATEGORY_PIZZA),
                product(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Pepperoni Pizza", "Spicy pepperoni pizza", 14.99, "https://www.hunts.com/sites/g/files/qyyrlu211/files/uploadedImages/img_6934_48664.jpg", DemoDataConstants.CATEGORY_PIZZA),
                product(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Coca Cola", "Cold drink", 2.99, "https://miamikosherfood.com/cdn/shop/files/049000050103.jpg?v=1765824810", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Lemonade", "Fresh lemon drink", 3.49, "https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Chocolate Cake", "Sweet dessert", 5.99, "https://sallysbakingaddiction.com/wp-content/uploads/2013/04/triple-chocolate-cake-4-600x900.jpg", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Classic Burger", "Beef burger with vegetables", 10.99, "https://homefoodie.com.ph/uploads/2024/CLASSIC%20BURGER.jpg", DemoDataConstants.CATEGORY_BURGERS),
                product(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Double Cheese Burger", "Double meat and cheese", 15.49, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRPl9lncUDCC6xQ0BJoi7pJDa_tDZLtxivy4w&s", DemoDataConstants.CATEGORY_BURGERS),
                product(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Fanta", "Orange soda", 2.49, "https://m.ebag.bg/en/products/4156/images/2/400", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Iced Tea", "Cold peach iced tea", 3.29, "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Brownie", "Chocolate brownie slice", 4.99, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Salmon Sushi Set", "Fresh salmon sushi", 18.99, "https://kayo.bg/sites/default/files/siomga_set.jpg", DemoDataConstants.CATEGORY_SUSHI),
                product(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Tuna Roll", "Tuna sushi roll", 16.49, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQbfV7iV6CZBb5VR_92P5pFHdcbtsNv8ABAKg&s", DemoDataConstants.CATEGORY_SUSHI),
                product(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Green Salad", "Fresh healthy salad", 7.99, "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chopped-green-salad-with-herby-chilli-dressing-429ab82.jpg?quality=90&resize=500,454", DemoDataConstants.CATEGORY_SALADS),
                product(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Seaweed Salad", "Light seaweed salad with sesame", 8.49, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),
                product(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Green Tea", "Warm Japanese green tea", 2.99, "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),

                product(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Tomato Pasta", "Pasta with tomato sauce and basil", 11.99, "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Creamy Mushroom Pasta", "Cream sauce pasta with mushrooms", 13.49, "https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Baked Penne", "Penne pasta baked with tomato sauce and cheese", 12.49, "https://www.thecandidcooks.com/wp-content/uploads/2021/01/baked-penne-feature.jpg", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Italian Soda", "Sparkling fruit soda", 3.49, "https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Tiramisu", "Classic coffee dessert", 6.49, "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Beef Taco", "Soft taco with seasoned beef", 8.99, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_TACOS),
                product(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Chicken Burrito", "Large burrito with chicken and rice", 11.99, "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_TACOS),
                product(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Loaded Nachos", "Crispy nachos with cheese and salsa", 7.99, "https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_TACOS),
                product(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Lime Soda", "Sparkling lime drink", 2.99, "https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Churros", "Warm churros with sugar", 5.49, "https://images.unsplash.com/photo-1624371414361-e670edf4898d?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Chicken Curry Bowl", "Chicken curry with rice", 13.99, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_CURRY),
                product(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Vegetable Curry Bowl", "Vegetable curry with herbs", 11.99, "https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_CURRY),
                product(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Mango Lassi", "Sweet yogurt mango drink", 4.49, "https://www.yellowthyme.com/wp-content/uploads/2023/03/Mango-Lassi-08589.jpg", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Masala Tea", "Warm spiced tea", 3.49, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Rice Pudding", "Creamy rice dessert", 5.49, "https://successrice.com/wp-content/uploads/2019/03/782_2062-1468x980.jpg", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Avocado Bowl", "Avocado bowl with grains", 10.99, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),
                product(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Quinoa Salad", "Quinoa with vegetables", 9.99, "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),
                product(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Chicken Protein Bowl", "Chicken, rice and greens", 12.49, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),
                product(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Fresh Orange Juice", "Freshly squeezed juice", 4.49, "https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Fruit Cup", "Seasonal fruit dessert", 5.49, "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Chocolate Waffle", "Waffle with chocolate cream", 7.99, "https://images.unsplash.com/photo-1562376552-0d160a2f238d?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),
                product(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Berry Pancakes", "Pancakes with berries", 8.49, "https://images.unsplash.com/photo-1528207776546-365bb710ee93?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),
                product(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Cheesecake", "Creamy cheesecake slice", 6.99, "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),
                product(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Iced Coffee", "Cold coffee drink", 4.49, "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Fruit Salad", "Fresh fruit salad", 5.99, "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),

                product(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Pesto Tagliatelle", "Fresh tagliatelle with basil pesto", 12.99, "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Spaghetti Carbonara", "Creamy carbonara with parmesan", 13.99, "https://images.unsplash.com/photo-1612874742237-6526221588e3?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Vegetable Lasagna", "Layered pasta with vegetables and cheese", 14.49, "https://images.unsplash.com/photo-1574894709920-11b28e7367e3?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PASTA),
                product(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Sparkling Water", "Cold sparkling mineral water", 2.49, "https://www.verywellfit.com/thmb/VbD55hPo0nuHLIxUSPMBuD3QqXo=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/sparklingwater-47f57bf0c5a14358bf820e1d999002fe.jpg", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Panna Cotta", "Vanilla panna cotta with berry sauce", 6.49, "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Chicken Doner Wrap", "Warm wrap with chicken doner and vegetables", 8.99, "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DONER),
                product(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Beef Doner Box", "Beef doner with fries and sauce", 10.99, "https://images.unsplash.com/photo-1529006557810-274b9b2fc783?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DONER),
                product(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Falafel Doner", "Falafel doner with salad and tahini", 8.49, "https://jetextramar.com/wp-content/uploads/2020/07/receta-de-salsa-de-yogur-fresca.jpg", DemoDataConstants.CATEGORY_DONER),
                product(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Ayran", "Cold yogurt drink", 2.49, "https://www.cubesnjuliennes.com/wp-content/uploads/2024/08/Turkish-Ayran-Drink-Recipe.jpg", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Shopska Salad", "Tomatoes, cucumbers and white cheese", 6.49, "https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SALADS),

                product(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Four Cheese Pizza", "Stone-baked pizza with four cheeses", 13.99, "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PIZZA),
                product(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "BBQ Chicken Pizza", "Pizza with chicken, barbecue sauce and red onion", 15.49, "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PIZZA),
                product(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Still Water", "500 ml bottled water", 1.99, "https://domf5oio6qrcr.cloudfront.net/medialibrary/7909/conversions/b8a1309a-ba53-48c7-bca3-9c36aab2338a-thumb.jpg", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Cola Zero", "330 ml can of cola zero", 2.49, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Mini Cannoli", "Two pastry rolls with sweet ricotta cream", 5.99, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Classic Smash Burger", "Smashed beef patty with cheddar and pickles", 11.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_BURGERS),
                product(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Crispy Chicken Burger", "Chicken burger with lettuce and garlic mayo", 10.99, "https://images.unsplash.com/photo-1606755962773-d324e0a13086?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_BURGERS),
                product(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Homemade Lemonade", "Fresh lemon drink in a 400 ml bottle", 3.49, "https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Peach Iced Tea", "Cold peach iced tea in a 330 ml bottle", 3.29, "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DRINKS),
                product(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Chocolate Muffin", "Soft muffin with chocolate chips", 4.49, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_DESSERTS),

                product(DemoDataConstants.SHOP_TECH_STORE_EMAIL, "Smartphone", "Latest Android phone", 699.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS9M4ydwp26-7mEzPMlRvh_j-p3gWwNV7vx9Q&s", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_TECH_STORE_EMAIL, "Laptop", "Gaming laptop", 1299.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSZWcgGtAEhOZRRofkCAdeOK3LPF7UCz_ZKxA&s", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_TECH_STORE_EMAIL, "Wireless Earbuds", "Compact bluetooth earbuds", 89.99, "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),

                product(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Milk", "Fresh milk", 1.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRdt7a0w7WPbJ25VQXpcAEO-Z5drxudGaRXpg&s", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Bread", "Fresh bread", 1.49, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRfvc5-n0439EwjQWopFuSfqK8y3Xv_BHSZYA&s", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Apples", "Fresh red apples", 3.49, "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Sunflower Bouquet", "Small fresh bouquet", 12.99, "https://images.unsplash.com/photo-1470509037663-253afd7f0f51?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_FLOWERS),
                product(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Vitamin C", "Daily vitamin tablets", 8.99, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PHARMACY),

                product(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Rose Bouquet", "Beautiful red roses", 25.00, "https://www.flowersforeveryone.com.au/cdn/shop/files/104376_FFE_Sh06_182.jpg?v=1769029217&width=1445", DemoDataConstants.CATEGORY_FLOWERS),
                product(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Tulips", "Fresh tulips", 18.00, "https://onlineflowerexpress.com/cdn/shop/files/PinkTulipsLove.webp?v=1691571050", DemoDataConstants.CATEGORY_FLOWERS),
                product(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Orchid Pot", "Elegant orchid plant", 29.99, "https://m.media-amazon.com/images/I/61-ahmwYrlL._AC_UF894,1000_QL80_.jpg", DemoDataConstants.CATEGORY_FLOWERS),
                product(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Flower Food", "Nutrient sachets for bouquets", 3.99, "https://images.unsplash.com/photo-1525310072745-f49212b5ac6d?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GARDENING),
                product(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Glass Vase", "Clear vase for fresh bouquets", 16.99, "https://www.grahamandgreen.co.uk/media/catalog/product/e/m/emi1010-blue-recycled-vase---detail_.jpg?quality=80&bg-color=255,255,255&fit=bounds&height=950&width=950&canvas=950:950", DemoDataConstants.CATEGORY_HOME_GOODS),

                product(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Pain Relief Tablets", "Basic pain relief tablets", 7.99, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Hand Sanitizer", "Pocket sanitizer bottle", 3.99, "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Vitamin D", "Daily vitamin supplement", 9.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRoqJbfLMdSW9rpv-C_u3leMIRbOt-9qu1GXg&s", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Honey Jar", "Natural honey", 6.49, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRFYreemWaLQN-QXF9ahXgtngFD-oNVjKY1hA&s", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Pet Wipes", "Gentle wipes for pets", 5.49, "https://befit.bg/media/catalog/product/cache/138c1f4c1b875d63f0f21638bf749075/8/4/840067_pawsy_pet_wipes_70_.jpg", DemoDataConstants.CATEGORY_PET_SUPPLIES),

                product(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Dog Food", "Dry food for dogs", 22.99, "https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PET_SUPPLIES),
                product(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Cat Toy", "Interactive toy for cats", 8.99, "https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PET_SUPPLIES),
                product(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Pet Shampoo", "Gentle shampoo for pets", 11.99, "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PET_SUPPLIES),
                product(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Pet Food Storage Box", "Airtight container for dry pet food", 16.99, "https://theyamazakihome.com/cdn/shop/products/voajonurgsig0iwmqcyt_1000x.jpg?v=1749157319", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Pet Balm", "Paw care balm", 7.49, "https://www.store-tipaw.com/cdn/shop/files/Background_1.webp?v=1768468694", DemoDataConstants.CATEGORY_PHARMACY),

                product(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Reading Lamp", "Small LED reading lamp", 19.99, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "E-reader Sleeve", "Protective sleeve for readers", 14.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFMBvb_rZrsbuHb_3aIQ1neD_RI_XlylCHUw&s", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Coffee Beans", "Coffee for reading sessions", 9.99, "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Bookmark Set", "Decorative bookmark set", 4.99, "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_BOOKS),
                product(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Paperback Novel", "Bestselling paperback novel", 12.49, "https://smartpress.com/resources/offering_images/paperback_books_3qtr_standing_front_bklt_depth_charge-20240726_131510127.jpg", DemoDataConstants.CATEGORY_BOOKS),

                product(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Fitness Tracker", "Smart tracker for workouts", 79.99, "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Wireless Headphones", "Headphones for training", 59.99, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Protein Bar Box", "Box of protein bars", 14.99, "https://images.unsplash.com/photo-1622484212850-eb596d769edc?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Sports Tape", "Support tape for training", 6.99, "https://m.media-amazon.com/images/I/81k9Z7-ySnL.jpg", DemoDataConstants.CATEGORY_SPORTS),
                product(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Yoga Mat", "Non-slip mat for home workouts", 18.99, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSymZspZ-EesSgqFF81aEa6Xmsu7hg0KI9bYQ&s", DemoDataConstants.CATEGORY_SPORTS),

                product(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Dish Soap", "Everyday dish soap", 3.49, "https://www.thebotanistdubai.com/cdn/shop/products/DishSoap500ml.png?v=1633507542", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Laundry Capsules", "Laundry detergent capsules", 11.99, "https://www.thespruce.com/thmb/deIthHQbxlrJ9b9KZOpaRIwepMM=/fit-in/1500x2667/filters:no_upscale():max_bytes(150000):strip_icc()/spr-tier-3-detail-tide-pods-ebrockob-001-1-1ee29c7f87e343fa9da06bad2ed0a284.jpeg", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "LED Bulb", "Energy saving bulb", 4.99, "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Storage Basket", "Simple home storage basket", 13.99, "https://www.thebasketcompany.com/images/rectangular-grey-buff-rattan-deep-wicker-storage-basket-p271-8349_zoom.jpg", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Table Flowers", "Small flowers for the table", 9.99, "https://images.unsplash.com/photo-1455659817273-f96807779a8a?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_FLOWERS),

                product(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Notebook Pack", "Set of lined notebooks", 8.99, "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_BOOKS),
                product(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Weekly Planner", "Planner for tasks and appointments", 11.49, "https://images.unsplash.com/photo-1506784365847-bbad939e9335?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_BOOKS),
                product(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Desk Lamp", "Adjustable LED desk lamp", 24.99, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Wireless Mouse", "Compact mouse for office work", 16.99, "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Pen Holder", "Metal holder for pens and pencils", 7.49, "https://m.media-amazon.com/images/I/71awyXSBwkL.jpg", DemoDataConstants.CATEGORY_HOME_GOODS),

                product(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Dumbbell Set", "Pair of adjustable dumbbells", 39.99, "https://images.unsplash.com/photo-1576678927484-cc907957088c?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_SPORTS),
                product(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Resistance Bands", "Set of resistance training bands", 14.99, "https://m.media-amazon.com/images/I/71p6dAKPG9L.jpg", DemoDataConstants.CATEGORY_SPORTS),
                product(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Jump Rope", "Speed rope for cardio workouts", 9.99, "https://www.pro-tecathletics.com/wp-content/uploads/2022/04/Premium-Jump-Rope-product-only.jpg", DemoDataConstants.CATEGORY_SPORTS),
                product(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Workout Tracker", "Smart tracker for daily workouts", 69.99, "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_ELECTRONICS),
                product(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Protein Shake", "Ready-to-drink protein shake", 3.99, "https://images.unsplash.com/photo-1622484212850-eb596d769edc?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),

                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Milk 1L", "Fresh whole milk in a 1 liter carton", 2.19, "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Free Range Eggs", "Pack of 10 free range eggs", 4.49, "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Sliced White Bread", "Soft sliced bread loaf", 1.89, "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Bananas 1kg", "Fresh bananas sold by the kilogram", 2.69, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Rice 1kg", "Long grain white rice", 3.29, "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_GROCERIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Dish Soap 500ml", "Lemon dishwashing liquid", 2.99, "https://island-ish.com/cdn/shop/products/image_550x652.webp?v=1648306860", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Paper Towels", "Two rolls of absorbent paper towels", 3.99, "https://www.scottbrand.com/-/media/feature/scott/na/us/products/product-detail/paper-towel/feature-3.webp?rev=f41805cf740540a4bf64a76c8854d197", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Trash Bags 30L", "Roll of 20 kitchen trash bags", 4.49, "https://shop.primepartner.eu/media/shop-primepartner-eu/.product-image/large/product/eu.erply.com/1299-4742002004742.png", DemoDataConstants.CATEGORY_HOME_GOODS),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Hand Sanitizer 100ml", "Pocket hand sanitizer bottle", 2.49, "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Vitamin C Tablets", "Pack of 20 vitamin C tablets", 5.99, "https://cloudinary.images-iherb.com/image/upload/f_auto,q_auto:eco/images/nwy/nwy40330/y/57.jpg", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Adhesive Bandages", "Box of 30 assorted bandages", 3.49, "https://images.unsplash.com/photo-1603398938378-e54eab446dde?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PHARMACY),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Dry Dog Food 2kg", "Chicken flavor dry food for dogs", 12.99, "https://images.unsplash.com/photo-1589924691995-400dc9ecc119?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PET_SUPPLIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Cat Litter 5L", "Clumping cat litter bag", 7.99, "https://images.unsplash.com/photo-1545249390-6bdfa286032f?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_PET_SUPPLIES),
                product(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Seasonal Bouquet", "Small mixed flower bouquet", 9.99, "https://images.unsplash.com/photo-1470509037663-253afd7f0f51?auto=format&fit=crop&w=1200&q=80", DemoDataConstants.CATEGORY_FLOWERS)
        );
    }

    private DemoProductRequest product(String merchantEmail, String name, String description, double price, String imageUrl, String categoryName) {
        return new DemoProductRequest(
                merchantEmail, name, description,
                BigDecimal.valueOf(price), imageUrl, categoryName
        );
    }
}
