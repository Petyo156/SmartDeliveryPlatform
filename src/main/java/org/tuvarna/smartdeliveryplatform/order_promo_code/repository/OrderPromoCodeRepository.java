package org.tuvarna.smartdeliveryplatform.order_promo_code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.order_promo_code.model.OrderPromoCode;

import java.util.UUID;

@Repository
public interface OrderPromoCodeRepository extends JpaRepository<OrderPromoCode, UUID> {
}