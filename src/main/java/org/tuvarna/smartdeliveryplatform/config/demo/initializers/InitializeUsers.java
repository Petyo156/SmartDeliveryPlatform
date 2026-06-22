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
                new DemoUserRequest(
                        DemoDataConstants.USER1_EMAIL,
                        "Petar",
                        "Petrov",
                        "0000000001"
                ),
                new DemoUserRequest(
                        DemoDataConstants.USER2_EMAIL,
                        "Koko",
                        "Kolev",
                        "0000000002"
                ),
                new DemoUserRequest(
                        DemoDataConstants.COURIER1_EMAIL,
                        "Kurier",
                        "Kurierov",
                        "0000000003"
                ),
                new DemoUserRequest(
                        DemoDataConstants.COURIER2_EMAIL,
                        "Courier",
                        "Courierv",
                        "0000000004"
                ),
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Pizza",
                        "Heaven",
                        "0000000005"
                ),
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Burger",
                        "Kingdom",
                        "0000000006"
                ),
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Sushi",
                        "World",
                        "0000000007"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Tech",
                        "Store",
                        "0000000008"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Fresh",
                        "Market",
                        "0000000009"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Flower",
                        "Shop",
                        "0000000010"
                )
        );
    }
}
