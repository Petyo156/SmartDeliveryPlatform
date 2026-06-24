package org.tuvarna.smartdeliveryplatform.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.courier.repository.CourierRepository;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderRepository;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderStatusHistoryRepository;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderWorkflowService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.AddCartItemRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OrderWorkflowITest {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private CourierService courierService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderWorkflowService orderWorkflowService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Test
    void merchantAndCourierCanMoveOrderThroughDeliveryWorkflow() {
        User client = registerUser("workflow.client@example.com", "0888123421");
        User merchantOwner = createOpenMerchantWithProducts();
        ProductResponse pizza = productNamed(merchantOwner.getEmail());
        User courierUser = createAvailableCourier("workflow.courier@example.com", "0888123423");
        Courier courier = courierRepository.findCourierByUser_Email(courierUser.getEmail()).orElseThrow();

        cartService.addProductToCart(client, cartRequest(pizza.getSlug()));
        orderService.placeOrder(client, newAddressOrderRequest());

        Order order = onlyOrderForClient();
        String orderNumber = order.getOrderNumber();
        User otherMerchant = createMerchant("workflow-other", "Workflow Other", "0888123424");
        assertThatThrownBy(() -> orderWorkflowService.acceptByMerchant(orderNumber, otherMerchant))
                .isInstanceOf(OrderNotFoundException.class);

        orderWorkflowService.acceptByMerchant(orderNumber, merchantOwner);

        Order acceptedOrder = onlyOrderForClient();
        assertThat(acceptedOrder.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(acceptedOrder.getCourier().getUser().getEmail()).isEqualTo("workflow.courier@example.com");
        assertThat(courierRepository.findById(courier.getId()).orElseThrow().getIsAvailable()).isFalse();

        User otherCourier = createAvailableCourier("workflow.other.courier@example.com", "0888123425");
        assertThatThrownBy(() -> orderWorkflowService.confirmByCourier(orderNumber, otherCourier))
                .isInstanceOf(OrderNotFoundException.class);

        orderWorkflowService.confirmByCourier(orderNumber, courierUser);
        orderWorkflowService.markPreparingByMerchant(orderNumber, merchantOwner);
        orderWorkflowService.markPreparedByMerchant(orderNumber, merchantOwner);
        orderWorkflowService.markOnTheWayByCourier(orderNumber, courierUser);
        orderWorkflowService.markDeliveredByCourier(orderNumber, courierUser);

        Order deliveredOrder = onlyOrderForClient();
        assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(orderStatusHistoryRepository.findAllByOrderOrderByChangedAtAsc(deliveredOrder))
                .extracting(OrderStatusHistory::getStatus)
                .containsExactly(
                        OrderStatus.PENDING,
                        OrderStatus.ACCEPTED,
                        OrderStatus.COURIER_ACCEPTED,
                        OrderStatus.PREPARING,
                        OrderStatus.PREPARED,
                        OrderStatus.ON_THE_WAY,
                        OrderStatus.DELIVERED
                );
        assertThat(courierRepository.findById(courier.getId()).orElseThrow().getIsAvailable()).isTrue();
    }

    private User createOpenMerchantWithProducts() {
        User owner = createMerchant("workflow", "Workflow", "0888123422");
        merchantService.toggleMerchantIsClosedStatus(owner.getEmail());

        categoryService.createMerchantCategory(authenticationFor(owner), "%s Main".formatted("Workflow"));
        UUID categoryId = categoryNamed(owner.getEmail(), "%s Main".formatted("Workflow")).getId();

        productService.createProduct(owner.getEmail(), productRequest(
                "%s Pizza".formatted("Workflow"),
                categoryId,
                "https://img.test/%s-pizza.png".formatted("workflow")
        ));

        return owner;
    }

    private User createMerchant(String slugPrefix, String displayName, String ownerPhoneNumber) {
        User owner = registerUser("%s.owner@example.com".formatted(slugPrefix), ownerPhoneNumber);
        adminService.makeUserMerchant(merchantRequest(owner.getEmail(), displayName, slugPrefix));
        return userRepository.findByEmail(owner.getEmail()).orElseThrow();
    }

    private User createAvailableCourier(String email, String phoneNumber) {
        registerUser(email, phoneNumber);
        adminService.makeUserCourier(email);
        courierService.toggleCourierAvailability(email);
        return userRepository.findByEmail(email).orElseThrow();
    }

    private User registerUser(String email, String phoneNumber) {
        userService.register(RegisterRequest.builder()
                .userRegisterRequest(UserRegisterRequest.builder()
                        .email(email)
                        .password("TestPassword123")
                        .confirmPassword("TestPassword123")
                        .firstName("Flow")
                        .lastName("User")
                        .phoneNumber(phoneNumber)
                        .build())
                .build());
        return userRepository.findByEmail(email).orElseThrow();
    }

    private MerchantRequest merchantRequest(String ownerEmail, String displayName, String slugPrefix) {
        return MerchantRequest.builder()
                .email(ownerEmail)
                .name("%s Merchant".formatted(displayName))
                .description("Integration test merchant")
                .type(MerchantType.RESTAURANT)
                .imageUrl("https://img.test/%s-merchant.png".formatted(slugPrefix))
                .address(AddressRequest.builder()
                        .city("Sofia")
                        .street("%s Merchant Street".formatted(displayName))
                        .building("1")
                        .build())
                .build();
    }

    private AuthenticationMetadata authenticationFor(User user) {
        return AuthenticationMetadata.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    private CategoryResponse categoryNamed(String merchantEmail, String name) {
        return categoryService.getMerchantAvailableCategories(merchantEmail)
                .stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }

    private ProductRequest productRequest(String name, UUID categoryId, String imageUrl) {
        return ProductRequest.builder()
                .name(name)
                .description("Integration test product")
                .price(new BigDecimal("7.50"))
                .categoryId(categoryId)
                .imageUrl(imageUrl)
                .build();
    }

    private ProductResponse productNamed(String merchantEmail) {
        return productService.getMerchantProductResponses(merchantEmail)
                .stream()
                .filter(product -> "Workflow Pizza".equals(product.getName()))
                .findFirst()
                .orElseThrow();
    }

    private AddCartItemRequest cartRequest(String productSlug) {
        return AddCartItemRequest.builder()
                .productSlug(productSlug)
                .quantity(1)
                .build();
    }

    private OrderPlacementRequest newAddressOrderRequest() {
        return OrderPlacementRequest.builder()
                .addressMode(CheckoutAddressMode.NEW)
                .city("Sofia")
                .street("Workflow Street")
                .building("20")
                .build();
    }

    private Order onlyOrderForClient() {
        List<Order> orders = orderRepository.findAllByClient_EmailOrderByCreatedAtDesc("workflow.client@example.com");
        assertThat(orders).hasSize(1);
        return orders.getFirst();
    }
}
