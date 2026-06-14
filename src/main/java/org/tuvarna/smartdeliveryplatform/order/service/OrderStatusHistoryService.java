package org.tuvarna.smartdeliveryplatform.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderStatusHistory;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderStatusHistoryRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStatusHistoryService {
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderStatusHistoryService(OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    public void saveOrderStatusHistory(Order order, OrderStatus status, User changedBy, LocalDateTime changedAt, String note) {
        OrderStatusHistory orderStatusHistory = initializeHistory(order, status, changedBy, changedAt, note);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getStatusHistory(Order order) {
        return orderStatusHistoryRepository.findAllByOrderOrderByChangedAtAsc(order);
    }

    private OrderStatusHistory initializeHistory(Order order, OrderStatus status, User changedBy, LocalDateTime changedAt, String note) {
        return OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .changedAt(changedAt)
                .changedBy(changedBy)
                .note(note)
                .build();
    }
}
