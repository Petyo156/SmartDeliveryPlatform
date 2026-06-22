package org.tuvarna.smartdeliveryplatform.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderCourierDecline;

import java.util.UUID;

@Repository
public interface OrderCourierDeclineRepository extends JpaRepository<OrderCourierDecline, UUID> {
    boolean existsByOrderAndCourier(Order order, Courier courier);
}
