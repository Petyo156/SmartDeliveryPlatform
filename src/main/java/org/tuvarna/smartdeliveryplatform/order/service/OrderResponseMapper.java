package org.tuvarna.smartdeliveryplatform.order.service;

import org.springframework.stereotype.Component;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderItem;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderItemResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderStatusHistoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderWorkflowActionsResponse;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderResponseMapper {
    public OrderSummaryResponse toClientSummary(Order order) {
        return toSummary(order, OrderWorkflowRules.clientActions());
    }

    public OrderSummaryResponse toMerchantSummary(Order order) {
        return toSummary(order, OrderWorkflowRules.merchantActions(order));
    }

    public OrderSummaryResponse toCourierSummary(Order order) {
        return toSummary(order, OrderWorkflowRules.courierActions(order));
    }

    public OrderDetailsResponse toClientDetails(Order order, List<OrderStatusHistory> statusHistory) {
        return toDetails(order, statusHistory, OrderWorkflowRules.clientActions());
    }

    public OrderDetailsResponse toMerchantDetails(Order order, List<OrderStatusHistory> statusHistory) {
        return toDetails(order, statusHistory, OrderWorkflowRules.merchantActions(order));
    }

    public OrderDetailsResponse toCourierDetails(Order order, List<OrderStatusHistory> statusHistory) {
        return toDetails(order, statusHistory, OrderWorkflowRules.courierActions(order));
    }

    private OrderSummaryResponse toSummary(Order order, OrderWorkflowActionsResponse actions) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .merchantName(formatMerchantName(order.getMerchant()))
                .merchantSlug(order.getMerchant().getSlug())
                .merchantImageUrl(order.getMerchant().getImageUrl())
                .merchantAddress(formatMerchantAddress(order.getMerchant()))
                .clientName(formatUserName(order.getClient()))
                .clientPhoneNumber(order.getClient().getPhoneNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .courierName(formatCourierName(order.getCourier()))
                .courierEmail(formatCourierEmail(order.getCourier()))
                .courierPhoneNumber(formatCourierPhoneNumber(order.getCourier()))
                .courierAssigned(order.getCourier() != null)
                .actions(actions)
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(formatDeliveryAddress(order))
                .createdAt(order.getCreatedAt())
                .items(List.of())
                .build();
    }

    private OrderDetailsResponse toDetails(Order order, List<OrderStatusHistory> statusHistory, OrderWorkflowActionsResponse actions) {
        return OrderDetailsResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .merchantName(formatMerchantName(order.getMerchant()))
                .merchantSlug(order.getMerchant().getSlug())
                .merchantImageUrl(order.getMerchant().getImageUrl())
                .merchantAddress(formatMerchantAddress(order.getMerchant()))
                .clientName(formatUserName(order.getClient()))
                .clientPhoneNumber(order.getClient().getPhoneNumber())
                .courierName(formatCourierName(order.getCourier()))
                .courierEmail(formatCourierEmail(order.getCourier()))
                .courierPhoneNumber(formatCourierPhoneNumber(order.getCourier()))
                .courierAssigned(order.getCourier() != null)
                .actions(actions)
                .deliveryAddress(formatDeliveryAddress(order))
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .toList())
                .statusHistory(statusHistory.stream()
                        .map(history -> toOrderStatusHistoryResponse(order, history))
                        .toList())
                .build();
    }

    private OrderStatusHistoryResponse toOrderStatusHistoryResponse(Order order, OrderStatusHistory statusHistory) {
        return OrderStatusHistoryResponse.builder()
                .status(statusHistory.getStatus())
                .changedAt(statusHistory.getChangedAt())
                .changedByName(formatHistoryActorName(order, statusHistory.getChangedBy()))
                .note(statusHistory.getNote())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal lineSubtotal = item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .productName(item.getProductNameAtOrder())
                .productImageUrl(item.getProductImageAtOrder())
                .quantity(item.getQuantity())
                .unitPrice(item.getPriceAtOrderTime())
                .lineSubtotal(lineSubtotal)
                .build();
    }

    private String formatHistoryActorName(Order order, User changedBy) {
        if (changedBy == null) {
            return null;
        }

        if (order.getMerchant().getUser().getId().equals(changedBy.getId())) {
            return formatMerchantName(order.getMerchant());
        }

        return formatUserName(changedBy);
    }

    private String formatUserName(User user) {
        return "%s %s".formatted(user.getFirstName(), user.getLastName());
    }

    private String formatMerchantName(Merchant merchant) {
        return merchant.getName();
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

    private String formatCourierName(Courier courier) {
        if (courier == null) {
            return "Not assigned yet";
        }

        return formatUserName(courier.getUser());
    }

    private String formatCourierEmail(Courier courier) {
        return courier == null ? null : courier.getUser().getEmail();
    }

    private String formatCourierPhoneNumber(Courier courier) {
        return courier == null ? null : courier.getUser().getPhoneNumber();
    }
}
