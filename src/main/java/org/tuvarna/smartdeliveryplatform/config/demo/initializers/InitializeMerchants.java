package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoMerchantRequest;

import java.util.List;

@Order(4)
@Component
@Slf4j
public class InitializeMerchants implements CommandLineRunner {

    private final MerchantService merchantService;
    private final AdminService adminService;

    public InitializeMerchants(MerchantService merchantService, AdminService adminService) {
        this.merchantService = merchantService;
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        initializeMerchants();
    }

    private void initializeMerchants() {
        if (merchantService.merchantCountMoreThanZero()) {
            return;
        }

        log.info("Initializing demo merchants...");
        List<DemoMerchantRequest> demoMerchantRequests = getDemoMerchants();
        demoMerchantRequests.forEach(this::registerMerchant);
        log.info("Demo merchants initialized successfully");
    }

    private void registerMerchant(DemoMerchantRequest request) {
        MerchantRequest merchantRequest = initializeMerchantRequest(request);
        adminService.makeUserMerchant(merchantRequest);
        merchantService.toggleMerchantIsClosedStatus(merchantRequest.getEmail());
    }

    private MerchantRequest initializeMerchantRequest(DemoMerchantRequest request) {
        return MerchantRequest.builder()
                .email(request.getEmail())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .imageUrl(request.getImageUrl())
                .address(
                        AddressRequest.builder()
                                .city(request.getCity())
                                .street(request.getStreet())
                                .building(request.getBuilding())
                                .build()
                )
                .build();
    }

    private List<DemoMerchantRequest> getDemoMerchants() {
        return List.of(
                merchant(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Pizza Heaven", "Best pizza in town", MerchantType.RESTAURANT,
                        "Sofia", "Main Street 1", "A", "https://www.allrecipes.com/thmb/kgZB2WpV5NUBsd0XPOkcOOV9SEY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/23290-pizza-dough-iii-VAT-Beauty-4x3-06192801c8fa48fe8afaadfea28f532b.jpg"),
                merchant(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Burger Kingdom", "Juicy burgers & fries", MerchantType.RESTAURANT,
                        "Varna", "Sea Street 12", "2", "https://www.allrecipes.com/thmb/vpth8WDEhejGg_pD7dQgWZVbjyQ=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/8667932-garlic-butter-burger-01-4x3-ccd6c1f3548b4aab83ae65dd4221bc7c.jpg"),
                merchant(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Sushi World", "Fresh sushi daily", MerchantType.RESTAURANT,
                        "Plovdiv", "Center Blvd 5", "10", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcST06ZcgTvbY_ICZxcwHRyv2I7awpWGlX6IXg&s"),
                merchant(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Pasta Corner", "Fresh pasta, sauces and Italian comfort food", MerchantType.RESTAURANT,
                        "Sofia", "Vitosha Blvd 24", "4", "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Taco Town", "Tacos, burritos and loaded nachos", MerchantType.RESTAURANT,
                        "Varna", "Mexico Street 7", "1", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Curry House", "Warm curries, rice bowls and naan", MerchantType.RESTAURANT,
                        "Plovdiv", "Spice Road 9", "B", "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Green Bowl", "Healthy salads, bowls and fresh juices", MerchantType.RESTAURANT,
                        "Burgas", "Garden Street 14", "2", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Sweet Tooth", "Cakes, waffles and handmade desserts", MerchantType.RESTAURANT,
                        "Sofia", "Dessert Lane 6", "8", "https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Pasta Factory", "Fresh pasta, sauces and Italian sides", MerchantType.RESTAURANT,
                        "Varna", "Pasta Street 15", "2", "https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Doner Point", "Fresh doner wraps, boxes and crisp sides", MerchantType.RESTAURANT,
                        "Plovdiv", "Kebab Lane 3", "7", "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Slice Station", "Stone-baked pizzas, drinks and quick desserts", MerchantType.RESTAURANT,
                        "Sofia", "Oven Street 12", "3", "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Burger Grill", "Grilled burgers, cold drinks and classic sweets", MerchantType.RESTAURANT,
                        "Burgas", "Grill Avenue 19", "5", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_TECH_STORE_EMAIL, "Tech Store", "Electronics and gadgets", MerchantType.SHOP,
                        "Sofia", "Tech Park 3", "1", "https://extension.harvard.edu/wp-content/uploads/sites/8/2024/07/tech.jpg"),
                merchant(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Fresh Market", "Groceries & fresh food", MerchantType.SHOP,
                        "Varna", "Market Street 8", "5", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_oyLoTngs8gVglF2pubqCiIFBX2to2N4uxg&s"),
                merchant(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Flower Shop", "Beautiful flowers daily", MerchantType.SHOP,
                        "Burgas", "Flower Ave 2", "3", "https://www.gardenia.net/wp-content/uploads/2023/05/types-of-flowers.webp"),
                merchant(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Pharmacy Plus", "Health essentials and pharmacy products", MerchantType.SHOP,
                        "Sofia", "Health Street 11", "1", "https://images.unsplash.com/photo-1587854692152-cbe660dbde88?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Pet Planet", "Food, toys and care products for pets", MerchantType.SHOP,
                        "Varna", "Pet Street 4", "6", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Book Nook", "Books, notebooks and reading accessories", MerchantType.SHOP,
                        "Plovdiv", "Library Blvd 18", "3", "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Sports Hub", "Sports gear, fitness accessories and equipment", MerchantType.SHOP,
                        "Burgas", "Arena Street 21", "5", "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Home Essentials", "Everyday home goods and household basics", MerchantType.SHOP,
                        "Sofia", "Home Park 10", "9", "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Office Corner", "Notebooks, desk tools and office essentials", MerchantType.SHOP,
                        "Varna", "Business Street 16", "4", "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Fitness Outlet", "Sports gear, trackers and workout snacks", MerchantType.SHOP,
                        "Plovdiv", "Training Blvd 22", "6", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80"),
                merchant(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Daily Grocer", "Everyday groceries, household basics and quick essentials", MerchantType.SHOP,
                        "Sofia", "Market Square 25", "1", "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=80")
        );
    }

    private DemoMerchantRequest merchant(String email, String name, String description, MerchantType type,
                                         String city, String street, String building, String imageUrl) {
        return new DemoMerchantRequest(
                email, name, description, type, city,
                street, building, imageUrl
        );
    }
}
