package org.tuvarna.smartdeliveryplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.demo.DemoMerchantRequest;

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
                new DemoMerchantRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Pizza Heaven",
                        "Best pizza in town",
                        MerchantType.RESTAURANT,
                        "Sofia",
                        "Main Street 1",
                        "A",
                        "/images/pizza.jpg"
                ),
                new DemoMerchantRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Burger Kingdom",
                        "Juicy burgers & fries",
                        MerchantType.RESTAURANT,
                        "Varna",
                        "Sea Street 12",
                        "2",
                        "/images/burger.jpg"
                ),
                new DemoMerchantRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Sushi World",
                        "Fresh sushi daily",
                        MerchantType.RESTAURANT,
                        "Plovdiv",
                        "Center Blvd 5",
                        "10",
                        "/images/sushi.jpg"
                ),
                new DemoMerchantRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Tech Store",
                        "Electronics and gadgets",
                        MerchantType.SHOP,
                        "Sofia",
                        "Tech Park 3",
                        "1",
                        "/images/tech.jpg"
                ),
                new DemoMerchantRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Fresh Market",
                        "Groceries & fresh food",
                        MerchantType.SHOP,
                        "Varna",
                        "Market Street 8",
                        "5",
                        "/images/groceries.jpg"
                ),
                new DemoMerchantRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Flower Shop",
                        "Beautiful flowers daily",
                        MerchantType.SHOP,
                        "Burgas",
                        "Flower Ave 2",
                        "3",
                        "/images/flowers.jpg"
                )
        );
    }
}