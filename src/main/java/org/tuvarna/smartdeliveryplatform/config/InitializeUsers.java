package org.tuvarna.smartdeliveryplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.demo.DemoUserRequest;

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
                .phoneNumber(DemoDataConstants.DEMO_PHONE)
                .password(DemoDataConstants.DEMO_PASSWORD)
                .confirmPassword(DemoDataConstants.DEMO_PASSWORD)
                .build();
    }

    private List<DemoUserRequest> getDemoUsers() {
        return List.of(
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_PIZZA_HEAVEN_EMAIL,
                        "Pizza",
                        "Heaven"
                ),
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_BURGER_KINGDOM_EMAIL,
                        "Burger",
                        "Kingdom"
                ),
                new DemoUserRequest(
                        DemoDataConstants.RESTAURANT_SUSHI_WORLD_EMAIL,
                        "Sushi",
                        "World"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_TECH_STORE_EMAIL,
                        "Tech",
                        "Store"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_FRESH_MARKET_EMAIL,
                        "Fresh",
                        "Market"
                ),
                new DemoUserRequest(
                        DemoDataConstants.SHOP_FLOWER_SHOP_EMAIL,
                        "Flower",
                        "Shop"
                )
        );
    }
}