package org.tuvarna.smartdeliveryplatform.config.demo.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoUserRequest;

import java.util.List;

@Order(2)
@Component
@Slf4j
public class InitializeUsers implements CommandLineRunner {
    private final UserService userService;

    public InitializeUsers(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        initializeUsers();
    }

    private void initializeUsers() {
        if (userService.userCountMoreThanOne()) {
            return;
        }
        log.info("Initializing demo users...");
        List<DemoUserRequest> demoUserRequests = getDemoUsers();
        demoUserRequests.forEach(this::registerUser);
        log.info("Demo users initialized successfully");
    }

    private void registerUser(DemoUserRequest demoUserRequestList) {
        UserRegisterRequest userRegisterRequest = initializeDemoUsersRegisterRequest(demoUserRequestList);
        RegisterRequest registerRequest = RegisterRequest.builder()
                .userRegisterRequest(userRegisterRequest)
                .build();

        userService.register(registerRequest);
    }

    private UserRegisterRequest initializeDemoUsersRegisterRequest(DemoUserRequest demoUserRequest) {
        return UserRegisterRequest.builder()
                .email(demoUserRequest.getEmail())
                .firstName(demoUserRequest.getFirstName())
                .lastName(demoUserRequest.getLastName())
                .phoneNumber(demoUserRequest.getPhoneNumber())
                .password(DemoDataConstants.DEMO_PASSWORD)
                .confirmPassword(DemoDataConstants.DEMO_PASSWORD)
                .build();
    }

    private List<DemoUserRequest> getDemoUsers() {
        return List.of(
                user(DemoDataConstants.USER1_EMAIL, "Petar", "Petrov", "0000000001"),
                user(DemoDataConstants.USER2_EMAIL, "Koko", "Kolev", "0000000002"),
                user(DemoDataConstants.COURIER1_EMAIL, "Kurier", "Kurierov", "0000000003"),
                user(DemoDataConstants.COURIER2_EMAIL, "Courier", "Courierv", "0000000004"),
                user(DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL, "Pizza", "Heaven", "0000000005"),
                user(DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL, "Burger", "Kingdom", "0000000006"),
                user(DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL, "Sushi", "World", "0000000007"),
                user(DemoDataConstants.RESTAURANT_PASTA_CORNER_EMAIL, "Pasta", "Corner", "0000000011"),
                user(DemoDataConstants.RESTAURANT_TACO_TOWN_EMAIL, "Taco", "Town", "0000000012"),
                user(DemoDataConstants.RESTAURANT_CURRY_HOUSE_EMAIL, "Curry", "House", "0000000013"),
                user(DemoDataConstants.RESTAURANT_GREEN_BOWL_EMAIL, "Green", "Bowl", "0000000014"),
                user(DemoDataConstants.RESTAURANT_SWEET_TOOTH_EMAIL, "Sweet", "Tooth", "0000000015"),
                user(DemoDataConstants.RESTAURANT_PASTA_FACTORY_EMAIL, "Pasta", "Factory", "0000000021"),
                user(DemoDataConstants.RESTAURANT_DONER_POINT_EMAIL, "Doner", "Point", "0000000022"),
                user(DemoDataConstants.RESTAURANT_SLICE_STATION_EMAIL, "Slice", "Station", "0000000025"),
                user(DemoDataConstants.RESTAURANT_BURGER_GRILL_EMAIL, "Burger", "Grill", "0000000026"),
                user(DemoDataConstants.SHOP_TECH_STORE_EMAIL, "Tech", "Store", "0000000008"),
                user(DemoDataConstants.SHOP_FRESH_MARKET_EMAIL, "Fresh", "Market", "0000000009"),
                user(DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL, "Flower", "Shop", "0000000010"),
                user(DemoDataConstants.SHOP_PHARMACY_PLUS_EMAIL, "Pharmacy", "Plus", "0000000016"),
                user(DemoDataConstants.SHOP_PET_PLANET_EMAIL, "Pet", "Planet", "0000000017"),
                user(DemoDataConstants.SHOP_BOOK_NOOK_EMAIL, "Book", "Nook", "0000000018"),
                user(DemoDataConstants.SHOP_SPORTS_HUB_EMAIL, "Sports", "Hub", "0000000019"),
                user(DemoDataConstants.SHOP_HOME_ESSENTIALS_EMAIL, "Home", "Essentials", "0000000020"),
                user(DemoDataConstants.SHOP_OFFICE_CORNER_EMAIL, "Office", "Corner", "0000000023"),
                user(DemoDataConstants.SHOP_FITNESS_OUTLET_EMAIL, "Fitness", "Outlet", "0000000024"),
                user(DemoDataConstants.SHOP_DAILY_GROCER_EMAIL, "Daily", "Grocer", "0000000027")
        );
    }

    private DemoUserRequest user(String email, String firstName, String lastName, String phoneNumber) {
        return new DemoUserRequest(
                email, firstName, lastName, phoneNumber
        );
    }
}
