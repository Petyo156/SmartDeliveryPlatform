package org.tuvarna.smartdeliveryplatform.courier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
    Optional<Courier> findCourierByUser_Email(String email);

    @Query(value = """
            SELECT c.*
            FROM couriers c
            WHERE c.is_active = true
              AND c.is_available = true
              AND c.is_busy = false
              AND NOT EXISTS (
                  SELECT 1
                  FROM orders o
                  WHERE o.courier_id = c.id
                    AND o.status IN (:activeStatuses)
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM order_courier_declines decline
                  WHERE decline.order_id = :orderId
                    AND decline.courier_id = c.id
              )
            ORDER BY c.id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<Courier> findFirstEligibleCourierForOrder(
            @Param("orderId") UUID orderId,
            @Param("activeStatuses") Collection<String> activeStatuses
    );
}
