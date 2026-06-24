package org.tuvarna.smartdeliveryplatform.integration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartItemRepository;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartRepository;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.category.repository.CategoryRepository;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.courier.repository.CourierRepository;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderItem;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderCourierDeclineRepository;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderItemRepository;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderRepository;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderStatusHistoryRepository;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.shared.enums.PaymentStatus;
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
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OrderITest {

    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private CourierRepository courierRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Autowired
    private OrderCourierDeclineRepository orderCourierDeclineRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        orderStatusHistoryRepository.deleteAllInBatch();
        orderCourierDeclineRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        merchantRepository.deleteAllInBatch();
        courierRepository.deleteAllInBatch();
        addressRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        entityManager.clear();
    }

    @Test
    void cartCheckoutPlacesPendingOrderWithSnapshotsHistoryAndClearsCart() {
        User client = registerUser("cart.client@example.com", "0888123411");
        User merchantOwner = createOpenMerchantWithProducts();
        ProductResponse pizza = productNamed(merchantOwner.getEmail(), "Cart Flow Pizza");
        ProductResponse pasta = productNamed(merchantOwner.getEmail(), "Cart Flow Pasta");

        cartService.addProductToCart(client, cartRequest(pizza.getSlug(), 2));
        cartService.addProductToCart(client, cartRequest(pasta.getSlug(), 1));

        orderService.placeOrder(client, newAddressOrderRequest());
        flushAndClear();

        Order order = onlyOrderForClient();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems())
                .extracting(OrderItem::getProductNameAtOrder, OrderItem::getProductImageAtOrder)
                .containsExactlyInAnyOrder(
                        tuple("Cart Flow Pizza", "https://img.test/cart-flow-pizza.png"),
                        tuple("Cart Flow Pasta", "https://img.test/cart-flow-pasta.png")
                );
        assertThat(itemNamed(order.getItems(), "Cart Flow Pizza").getPriceAtOrderTime())
                .isEqualByComparingTo("7.50");
        assertThat(itemNamed(order.getItems(), "Cart Flow Pasta").getPriceAtOrderTime())
                .isEqualByComparingTo("8.25");

        assertThat(orderStatusHistoryRepository.findAllByOrderOrderByChangedAtAsc(order))
                .extracting(OrderStatusHistory::getStatus)
                .containsExactly(OrderStatus.PENDING);

        Cart cart = cartRepository.findByUser_Email("cart.client@example.com").orElseThrow();
        assertThat(cart.getItems()).isEmpty();
        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    private User createOpenMerchantWithProducts() {
        User owner = registerUser("%s.owner@example.com".formatted("cart-flow"), "0888123412");
        adminService.makeUserMerchant(merchantRequest(owner.getEmail()));
        merchantService.toggleMerchantIsClosedStatus(owner.getEmail());

        categoryService.createMerchantCategory(authenticationFor(owner), "%s Main".formatted("Cart Flow"));
        UUID categoryId = categoryNamed(owner.getEmail(), "%s Main".formatted("Cart Flow")).getId();

        productService.createProduct(owner.getEmail(), productRequest(
                "%s Pizza".formatted("Cart Flow"),
                "7.50",
                categoryId,
                "https://img.test/%s-pizza.png".formatted("cart-flow")
        ));
        productService.createProduct(owner.getEmail(), productRequest(
                "%s Pasta".formatted("Cart Flow"),
                "8.25",
                categoryId,
                "https://img.test/%s-pasta.png".formatted("cart-flow")
        ));

        return owner;
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

    private MerchantRequest merchantRequest(String ownerEmail) {
        return MerchantRequest.builder()
                .email(ownerEmail)
                .name("%s Merchant".formatted("Cart Flow"))
                .description("Integration test merchant")
                .type(MerchantType.RESTAURANT)
                .imageUrl("https://img.test/%s-merchant.png".formatted("cart-flow"))
                .address(AddressRequest.builder()
                        .city("Sofia")
                        .street("%s Merchant Street".formatted("Cart Flow"))
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

    private ProductRequest productRequest(String name, String price, UUID categoryId, String imageUrl) {
        return ProductRequest.builder()
                .name(name)
                .description("Integration test product")
                .price(new BigDecimal(price))
                .categoryId(categoryId)
                .imageUrl(imageUrl)
                .build();
    }

    private ProductResponse productNamed(String merchantEmail, String name) {
        return productService.getMerchantProductResponses(merchantEmail)
                .stream()
                .filter(product -> name.equals(product.getName()))
                .findFirst()
                .orElseThrow();
    }

    private AddCartItemRequest cartRequest(String productSlug, int quantity) {
        return AddCartItemRequest.builder()
                .productSlug(productSlug)
                .quantity(quantity)
                .build();
    }

    private OrderPlacementRequest newAddressOrderRequest() {
        return OrderPlacementRequest.builder()
                .addressMode(CheckoutAddressMode.NEW)
                .city("Sofia")
                .street("Checkout Street")
                .building("10")
                .build();
    }

    private Order onlyOrderForClient() {
        List<Order> orders = orderRepository.findAllByClient_EmailOrderByCreatedAtDesc("cart.client@example.com");
        assertThat(orders).hasSize(1);
        return orders.getFirst();
    }

    private OrderItem itemNamed(List<OrderItem> items, String productName) {
        return items.stream()
                .filter(item -> productName.equals(item.getProductNameAtOrder()))
                .findFirst()
                .orElseThrow();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
