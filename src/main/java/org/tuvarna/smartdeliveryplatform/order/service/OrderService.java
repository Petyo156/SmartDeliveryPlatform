package org.tuvarna.smartdeliveryplatform.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.model.CartItem;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderItem;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderRepository;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderStatusHistoryRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.shared.enums.PaymentStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderItemResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderStatusHistoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {
    private static final BigDecimal PHASE_ONE_DELIVERY_FEE = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartService cartService;
    private final AddressService addressService;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository,
                        CartService cartService,
                        AddressService addressService) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.cartService = cartService;
        this.addressService = addressService;
    }

    @Transactional
    public void placeOrder(User user, OrderPlacementRequest request) {
        List<CartItem> cartItems = cartService.getValidatedCartItemsForCheckout(user);
        Merchant merchant = getMerchantFromCartItems(cartItems);

        validateMerchantCanAcceptOrders(merchant);

        Address address = addressService.resolveCheckoutAddress(user, request);
        LocalDateTime localDateTime = LocalDateTime.now();
        BigDecimal subtotal = calculateSubtotal(cartItems);
        Order order = createOrder(user, merchant, address, subtotal, localDateTime);
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> createOrderItem(order, cartItem))
                .toList();

        order.setItems(new ArrayList<>(orderItems));
        Order savedOrder = orderRepository.saveAndFlush(order);

        OrderStatusHistory orderStatusHistory = createOrderStatusHistory(savedOrder, user, localDateTime);
        orderStatusHistoryRepository.saveAndFlush(orderStatusHistory);

        cartService.clearCartItems(user);

        log.info("Created order {} for user {}", savedOrder.getOrderNumber(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersForUser(String email) {
        return orderRepository.findAllByClient_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toOrderSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersForMerchant(String merchantOwnerEmail) {
        return orderRepository.findAllByMerchant_User_EmailOrderByCreatedAtDesc(merchantOwnerEmail)
                .stream()
                .map(this::toOrderSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetailsForUser(String orderNumber, String email) {
        Order order = orderRepository.findByOrderNumberAndClient_Email(orderNumber, email)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));

        return toOrderDetailsResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetailsForMerchant(String orderNumber, String merchantOwnerEmail) {
        Order order = orderRepository.findByOrderNumberAndMerchant_User_Email(orderNumber, merchantOwnerEmail)
                .orElseThrow(() -> new OrderNotFoundException("Order not found."));

        return toOrderDetailsResponse(order);
    }

    public OrderPlacementRequest initializeOrderPlacementRequest(List<UserAddressResponse> addresses) {
        if (addresses.isEmpty()) {
            return OrderPlacementRequest.builder()
                    .addressMode(CheckoutAddressMode.NEW)
                    .build();
        }

        UserAddressResponse defaultAddress = addresses.stream()
                .filter(UserAddressResponse::isDefault)
                .findFirst()
                .orElse(addresses.getFirst());

        return OrderPlacementRequest.builder()
                .addressMode(CheckoutAddressMode.EXISTING)
                .addressId(UUID.fromString(defaultAddress.getId()))
                .build();
    }

    private Merchant getMerchantFromCartItems(List<CartItem> cartItems) {
        return cartItems.getFirst().getProduct().getMerchant();
    }

    private void validateMerchantCanAcceptOrders(Merchant merchant) {
        if (!Boolean.TRUE.equals(merchant.getIsActive())) {
            throw new OrderOperationException("This merchant is not available.");
        }

        if (Boolean.TRUE.equals(merchant.getIsClosed())) {
            throw new OrderOperationException("This merchant is currently closed.");
        }
    }

    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::calculateCartItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCartItemSubtotal(CartItem cartItem) {
        return cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    private Order createOrder(User user, Merchant merchant, Address address, BigDecimal subtotal, LocalDateTime localDateTime) {
        return Order.builder()
                .client(user)
                .merchant(merchant)
                .deliveryCity(address.getCity())
                .deliveryStreet(address.getStreet())
                .deliveryBuilding(address.getBuilding())
                .deliveryLat(address.getLat())
                .deliveryLng(address.getLng())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .deliveryFee(PHASE_ONE_DELIVERY_FEE)
                .totalPrice(subtotal.add(PHASE_ONE_DELIVERY_FEE))
                .createdAt(localDateTime)
                .updatedAt(localDateTime)
                .orderNumber(generateOrderNumber())
                .paymentStatus(PaymentStatus.PENDING)
                .items(new ArrayList<>())
                .build();
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        return OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .priceAtOrderTime(cartItem.getProduct().getPrice())
                .productNameAtOrder(cartItem.getProduct().getName())
                .productImageAtOrder(cartItem.getProduct().getImageUrl())
                .build();
    }

    private OrderStatusHistory createOrderStatusHistory(Order order, User changedBy, LocalDateTime now) {
        return OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PENDING)
                .changedAt(now)
                .changedBy(changedBy)
                .note("Order placed")
                .build();
    }

    private String generateOrderNumber() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "ORD-%s-%s".formatted(
                LocalDateTime.now().format(dateTimeFormatter),
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase()
        );
    }

    private OrderSummaryResponse toOrderSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .merchantName(order.getMerchant().getName())
                .merchantAddress(formatMerchantAddress(order.getMerchant()))
                .clientName(order.getClient().getFirstName() + " " + order.getClient().getLastName())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(formatDeliveryAddress(order))
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .toList())
                .build();
    }

    private OrderDetailsResponse toOrderDetailsResponse(Order order) {
        return OrderDetailsResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .merchantName(order.getMerchant().getName())
                .merchantAddress(formatMerchantAddress(order.getMerchant()))
                .clientName(order.getClient().getFirstName() + " " + order.getClient().getLastName())
                .deliveryAddress(formatDeliveryAddress(order))
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .toList())
                .statusHistory(orderStatusHistoryRepository.findAllByOrderOrderByChangedAtAsc(order)
                        .stream()
                        .map(this::toOrderStatusHistoryResponse)
                        .toList())
                .build();
    }

    private OrderStatusHistoryResponse toOrderStatusHistoryResponse(OrderStatusHistory statusHistory) {
        User changedBy = statusHistory.getChangedBy();
        String changedByName = changedBy == null
                ? null
                : changedBy.getFirstName() + " " + changedBy.getLastName();

        return OrderStatusHistoryResponse.builder()
                .status(statusHistory.getStatus())
                .changedAt(statusHistory.getChangedAt())
                .changedByName(changedByName)
                .note(statusHistory.getNote())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal lineSubtotal = item.getPriceAtOrderTime()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .productName(item.getProductNameAtOrder())
                .productImageUrl(item.getProductImageAtOrder())
                .quantity(item.getQuantity())
                .unitPrice(item.getPriceAtOrderTime())
                .lineSubtotal(lineSubtotal)
                .build();
    }

    private String formatDeliveryAddress(Order order) {
        return "%s, %s, %s".formatted(
                order.getDeliveryCity(),
                order.getDeliveryStreet(),
                order.getDeliveryBuilding()
        );
    }

    private String formatMerchantAddress(Merchant merchant) {
        Address address = merchant.getAddress();
        return "%s, %s, %s".formatted(
                address.getCity(),
                address.getStreet(),
                address.getBuilding()
        );
    }
}
