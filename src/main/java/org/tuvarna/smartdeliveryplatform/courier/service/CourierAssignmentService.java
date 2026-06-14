package org.tuvarna.smartdeliveryplatform.courier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.courier.repository.CourierRepository;
import org.tuvarna.smartdeliveryplatform.exception.CourierAssignmentException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.model.OrderCourierDecline;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderCourierDeclineRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CourierAssignmentService {
    private final CourierRepository courierRepository;
    private final OrderCourierDeclineRepository orderCourierDeclineRepository;

    public CourierAssignmentService(CourierRepository courierRepository,
                                    OrderCourierDeclineRepository orderCourierDeclineRepository) {
        this.courierRepository = courierRepository;
        this.orderCourierDeclineRepository = orderCourierDeclineRepository;
    }

    @Transactional
    public Courier assignAvailableCourier(Order order) {
        return findEligibleCourier(order)
                .map(this::assignCourier)
                .orElseThrow(() -> new CourierAssignmentException(ExceptionMessages.NO_AVAILABLE_COURIER));
    }

    @Transactional
    public Optional<Courier> tryAssignReplacementCourier(Order order) {
        return findEligibleCourier(order)
                .map(this::assignCourier);
    }

    @Transactional
    public void recordDecline(Order order, Courier courier, LocalDateTime declinedAt) {
        if (orderCourierDeclineRepository.existsByOrderAndCourier(order, courier)) {
            return;
        }

        orderCourierDeclineRepository.save(OrderCourierDecline.builder()
                .order(order)
                .courier(courier)
                .declinedAt(declinedAt)
                .build());
    }

    @Transactional
    public Courier releaseCourier(Courier courier) {
        courier.setIsAvailable(Boolean.TRUE.equals(courier.getIsActive()));
        Courier savedCourier = courierRepository.save(courier);
        log.info("Released courier {}", savedCourier.getUser().getEmail());
        return savedCourier;
    }

    private Optional<Courier> findEligibleCourier(Order order) {
        return courierRepository.findFirstEligibleCourierForOrder(
                order.getId(),
                activeAssignedStatusNames()
        );
    }

    private Courier assignCourier(Courier courier) {
        courier.setIsAvailable(false);
        Courier savedCourier = courierRepository.save(courier);
        log.info("Assigned courier {}", savedCourier.getUser().getEmail());
        return savedCourier;
    }

    private List<String> activeAssignedStatusNames() {
        return OrderStatus.activeAssignedStatuses()
                .stream()
                .map(OrderStatus::name)
                .toList();
    }
}
