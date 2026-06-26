package org.tuvarna.smartdeliveryplatform.order.service;

import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderWorkflowActionsResponse;
import java.util.List;

public final class OrderWorkflowRules {
    static boolean isFinal(OrderStatus status) {
        return switch (status) {
            case DELIVERED, CANCELLED, DECLINED -> true;
            case PENDING, ACCEPTED, COURIER_ACCEPTED, PREPARING, PREPARED, ON_THE_WAY -> false;
        };
    }

    static boolean canMerchantCancel(OrderStatus status) {
        return switch (status) {
            case PENDING, ACCEPTED, COURIER_ACCEPTED, PREPARING, PREPARED -> true;
            case ON_THE_WAY, DELIVERED, CANCELLED, DECLINED -> false;
        };
    }

    static OrderWorkflowActionsResponse clientActions() {
        return OrderWorkflowActionsResponse.builder().build();
    }

    static OrderWorkflowActionsResponse merchantActions(Order order) {
        return OrderWorkflowActionsResponse.builder()
                .canAcceptByMerchant(order.getStatus() == OrderStatus.PENDING)
                .canCancelByMerchant(canMerchantCancel(order.getStatus()))
                .canMarkPreparingByMerchant(order.getStatus() == OrderStatus.COURIER_ACCEPTED)
                .canMarkPreparedByMerchant(order.getStatus() == OrderStatus.PREPARING)
                .build();
    }

    static OrderWorkflowActionsResponse courierActions(Order order) {
        return OrderWorkflowActionsResponse.builder()
                .canConfirmByCourier(order.getStatus() == OrderStatus.ACCEPTED)
                .canDeclineByCourier(order.getStatus() == OrderStatus.ACCEPTED)
                .canMarkOnTheWayByCourier(order.getStatus() == OrderStatus.PREPARED)
                .canMarkDeliveredByCourier(order.getStatus() == OrderStatus.ON_THE_WAY)
                .build();
    }

    static List<OrderStatus> merchantActiveOrderStatuses() {
        return List.of(
                OrderStatus.PENDING,
                OrderStatus.ACCEPTED,
                OrderStatus.COURIER_ACCEPTED,
                OrderStatus.PREPARING,
                OrderStatus.PREPARED,
                OrderStatus.ON_THE_WAY
        );
    }
}
