package org.tuvarna.smartdeliveryplatform.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.order.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByClient_EmailOrderByCreatedAtDesc(String email);
    List<Order> findAllByMerchant_User_EmailOrderByCreatedAtDesc(String email);
    List<Order> findAllByCourier_User_EmailOrderByCreatedAtDesc(String email);
    Optional<Order> findByOrderNumberAndClient_Email(String orderNumber, String email);
    Optional<Order> findByOrderNumberAndMerchant_User_Email(String orderNumber, String email);
    Optional<Order> findByOrderNumberAndCourier_User_Email(String orderNumber, String email);
}
