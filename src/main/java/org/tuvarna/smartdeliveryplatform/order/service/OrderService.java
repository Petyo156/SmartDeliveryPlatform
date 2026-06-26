package org.tuvarna.smartdeliveryplatform.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.model.CartItem;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.shared.constants.ErrorMessages;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderItem;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.shared.enums.PaymentStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;
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
    private static final DateTimeFormatter ORDER_NUMBER_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final OrderResponseMapper orderResponseMapper;
    private final CartService cartService;
    private final AddressService addressService;
    private final OrderPricingService orderPricingService;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryService orderStatusHistoryService,
                        OrderResponseMapper orderResponseMapper,
                        CartService cartService,
                        AddressService addressService,
                        OrderPricingService orderPricingService) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryService = orderStatusHistoryService;
        this.orderResponseMapper = orderResponseMapper;
        this.cartService = cartService;
        this.addressService = addressService;
        this.orderPricingService = orderPricingService;
    }

    @Transactional
    public void placeOrder(User user, OrderPlacementRequest request) {
        List<CartItem> cartItems = cartService.getValidatedCartItemsForCheckout(user);
        Merchant merchant = getMerchantFromCartItems(cartItems);

        validateMerchantCanAcceptOrders(merchant);
        BigDecimal subtotal = calculateSubtotal(cartItems);
        validateMinimumOrderAmount(subtotal);

        Address address = addressService.resolveCheckoutAddress(user, request);
        LocalDateTime localDateTime = LocalDateTime.now();
        Order order = createOrder(user, merchant, address, subtotal, localDateTime);
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> createOrderItem(order, cartItem))
                .toList();
        order.setItems(new ArrayList<>(orderItems));

        Order savedOrder = orderRepository.save(order);
        orderStatusHistoryService.saveOrderStatusHistory(savedOrder, OrderStatus.PENDING, user,
                                                        localDateTime, SuccessMessages.ORDER_PLACED_HISTORY_NOTE);
        cartService.clearCartItems(user);
        log.info("Created order {} for user {}", savedOrder.getOrderNumber(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersForUser(String email) {
        return orderRepository.findAllByClient_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(orderResponseMapper::toClientSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersForMerchant(String merchantOwnerEmail) {
        return orderRepository.findAllByMerchant_User_EmailOrderByCreatedAtDesc(merchantOwnerEmail)
                .stream()
                .map(orderResponseMapper::toMerchantSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrdersForCourier(String courierEmail) {
        return orderRepository.findAllByCourier_User_EmailOrderByCreatedAtDesc(courierEmail)
                .stream()
                .map(orderResponseMapper::toCourierSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetailsForUser(String orderNumber, String email) {
        Order order = orderRepository.findByOrderNumberAndClient_Email(orderNumber, email)
                .orElseThrow(() -> new OrderNotFoundException(ErrorMessages.ORDER_NOT_FOUND));
        List<OrderStatusHistory> statusHistory = orderStatusHistoryService.getStatusHistory(order);

        return orderResponseMapper.toClientDetails(order, statusHistory);
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetailsForMerchant(String orderNumber, String merchantOwnerEmail) {
        Order order = orderRepository.findByOrderNumberAndMerchant_User_Email(orderNumber, merchantOwnerEmail)
                .orElseThrow(() -> new OrderNotFoundException(ErrorMessages.ORDER_NOT_FOUND));
        List<OrderStatusHistory> statusHistory = orderStatusHistoryService.getStatusHistory(order);

        return orderResponseMapper.toMerchantDetails(order, statusHistory);
    }

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetailsForCourier(String orderNumber, String courierEmail) {
        Order order = orderRepository.findByOrderNumberAndCourier_User_Email(orderNumber, courierEmail)
                .orElseThrow(() -> new OrderNotFoundException(ErrorMessages.ORDER_NOT_FOUND));
        List<OrderStatusHistory> statusHistory = orderStatusHistoryService.getStatusHistory(order);

        return orderResponseMapper.toCourierDetails(order, statusHistory);
    }

    public OrderPlacementRequest initializeOrderPlacementRequest(List<UserAddressResponse> addresses) {
        if (addresses.isEmpty()) {
            return OrderPlacementRequest.builder().addressMode(CheckoutAddressMode.NEW).build();
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

    @Transactional(readOnly = true)
    public boolean courierHasActiveAssignedOrders(String courierEmail) {
        return orderRepository.existsByCourier_User_EmailAndStatusIn(
                courierEmail,
                OrderStatus.activeAssignedStatuses()
        );
    }

    @Transactional(readOnly = true)
    public boolean merchantHasActiveOrders(String merchantEmail) {
        return orderRepository.existsByMerchant_User_EmailAndStatusIn(merchantEmail, OrderWorkflowRules.merchantActiveOrderStatuses());
    }

    private Merchant getMerchantFromCartItems(List<CartItem> cartItems) {
        return cartItems.getFirst().getProduct().getMerchant();
    }

    private void validateMerchantCanAcceptOrders(Merchant merchant) {
        if (!Boolean.TRUE.equals(merchant.getIsActive())) {
            throw new OrderOperationException(ErrorMessages.MERCHANT_IS_NOT_AVAILABLE);
        }

        if (Boolean.TRUE.equals(merchant.getIsClosed())) {
            throw new OrderOperationException(ErrorMessages.MERCHANT_IS_CURRENTLY_CLOSED);
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

    private void validateMinimumOrderAmount(BigDecimal subtotal) {
        if (subtotal.compareTo(OrderPricingService.MINIMUM_ORDER_AMOUNT) < 0) {
            throw new OrderOperationException(
                    ErrorMessages.MINIMUM_ORDER_AMOUNT_REQUIRED.formatted(OrderPricingService.MINIMUM_ORDER_AMOUNT)
            );
        }
    }

    private Order createOrder(User user, Merchant merchant, Address address, BigDecimal subtotal, LocalDateTime localDateTime) {
        return Order.builder()
                .client(user)
                .merchant(merchant)
                .deliveryCity(address.getCity())
                .deliveryStreet(address.getStreet())
                .deliveryBuilding(address.getBuilding())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .deliveryFee(OrderPricingService.DEFAULT_DELIVERY_FEE)
                .totalPrice(orderPricingService.calculateTotal(subtotal))
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

    private String generateOrderNumber() {
        return "ORD-%s-%s".formatted(
                LocalDateTime.now().format(ORDER_NUMBER_TIMESTAMP_FORMAT),
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase()
        );
    }
}
