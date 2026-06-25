package org.tuvarna.smartdeliveryplatform.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierAssignmentService;
import org.tuvarna.smartdeliveryplatform.exception.CourierOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantOrderWorkflowException;
import org.tuvarna.smartdeliveryplatform.exception.OrderNotFoundException;
import org.tuvarna.smartdeliveryplatform.order.model.Order;
import org.tuvarna.smartdeliveryplatform.order.repository.OrderRepository;
import org.tuvarna.smartdeliveryplatform.shared.constants.ErrorMessages;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class OrderWorkflowService {
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final CourierAssignmentService courierAssignmentService;

    public OrderWorkflowService(OrderRepository orderRepository,
                                OrderStatusHistoryService orderStatusHistoryService,
                                CourierAssignmentService courierAssignmentService) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryService = orderStatusHistoryService;
        this.courierAssignmentService = courierAssignmentService;
    }

    @Transactional
    public void acceptByMerchant(String orderNumber, User merchantUser) {
        Order order = getOrderForMerchantWorkflow(orderNumber, merchantUser);
        ensureMerchantCanAccept(order);

        Courier courier = courierAssignmentService.assignAvailableCourier(order);
        order.setCourier(courier);

        changeOrderStatus(order, OrderStatus.ACCEPTED, merchantUser, OrderWorkflowNotes.merchantAccepted(courier));

        log.info("Merchant {} accepted order {} and assigned courier {}",
                merchantUser.getEmail(),
                order.getOrderNumber(),
                courier.getUser().getEmail());
    }

    @Transactional
    public void cancelByMerchant(String orderNumber, User merchantUser) {
        Order order = getOrderForMerchantWorkflow(orderNumber, merchantUser);
        ensureMerchantCanCancel(order);

        Courier releasedCourier = releaseAssignedCourierIfPresent(order);
        String note = OrderWorkflowNotes.merchantCancelled(releasedCourier);
        changeOrderStatus(order, OrderStatus.CANCELLED, merchantUser, note);

        log.info("Merchant {} cancelled order {}", merchantUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void confirmByCourier(String orderNumber, User courierUser) {
        Order order = getOrderForCourierWorkflow(orderNumber, courierUser);
        ensureCourierCanConfirm(order);

        courierAssignmentService.markCourierBusy(order.getCourier());
        changeOrderStatus(order, OrderStatus.COURIER_ACCEPTED, courierUser, SuccessMessages.COURIER_ACCEPTED_HISTORY_NOTE);

        log.info("Courier {} confirmed order {}", courierUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void declineByCourier(String orderNumber, User courierUser) {
        Order order = getOrderForCourierWorkflow(orderNumber, courierUser);
        ensureCourierCanDecline(order);

        Courier declinedCourier = order.getCourier();
        processCourierDecline(order, courierUser, declinedCourier);

        log.info("Courier {} declined order {}", courierUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void markPreparingByMerchant(String orderNumber, User merchantUser) {
        Order order = getOrderForMerchantWorkflow(orderNumber, merchantUser);
        ensureMerchantCanMarkPreparing(order);

        changeOrderStatus(order, OrderStatus.PREPARING, merchantUser, SuccessMessages.MERCHANT_PREPARING_HISTORY_NOTE);

        log.info("Merchant {} marked order {} as preparing", merchantUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void markPreparedByMerchant(String orderNumber, User merchantUser) {
        Order order = getOrderForMerchantWorkflow(orderNumber, merchantUser);
        ensureMerchantCanMarkPrepared(order);

        changeOrderStatus(order, OrderStatus.PREPARED, merchantUser, SuccessMessages.MERCHANT_PREPARED_HISTORY_NOTE);

        log.info("Merchant {} marked order {} as prepared", merchantUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void markOnTheWayByCourier(String orderNumber, User courierUser) {
        Order order = getOrderForCourierWorkflow(orderNumber, courierUser);
        ensureCourierCanMarkOnTheWay(order);

        changeOrderStatus(order, OrderStatus.ON_THE_WAY, courierUser, SuccessMessages.COURIER_ON_THE_WAY_HISTORY_NOTE);

        log.info("Courier {} marked order {} as on the way", courierUser.getEmail(), order.getOrderNumber());
    }

    @Transactional
    public void markDeliveredByCourier(String orderNumber, User courierUser) {
        Order order = getOrderForCourierWorkflow(orderNumber, courierUser);
        ensureCourierCanMarkDelivered(order);

        Courier releasedCourier = releaseAssignedCourierIfPresent(order);
        String note = OrderWorkflowNotes.courierDelivered(releasedCourier);
        changeOrderStatus(order, OrderStatus.DELIVERED, courierUser, note);

        log.info("Courier {} delivered order {}", courierUser.getEmail(), order.getOrderNumber());
    }

    private Order getOrderForMerchantWorkflow(String orderNumber, User merchantUser) {
        return orderRepository.findByOrderNumberAndMerchant_User_Email(orderNumber, merchantUser.getEmail())
                .orElseThrow(() -> new OrderNotFoundException(ErrorMessages.ORDER_NOT_FOUND));
    }

    private Order getOrderForCourierWorkflow(String orderNumber, User courierUser) {
        return orderRepository.findByOrderNumberAndCourier_User_Email(orderNumber, courierUser.getEmail())
                .orElseThrow(() -> new OrderNotFoundException(ErrorMessages.ORDER_NOT_FOUND));
    }

    private void changeOrderStatus(Order order, OrderStatus status, User changedBy, String note) {
        changeOrderStatus(order, status, changedBy, LocalDateTime.now(), note);
    }

    private void changeOrderStatus(Order order, OrderStatus status, User changedBy, LocalDateTime changedAt, String note) {
        order.setStatus(status);
        order.setUpdatedAt(changedAt);

        orderRepository.save(order);
        orderStatusHistoryService.saveOrderStatusHistory(order, status, changedBy, changedAt, note);
    }

    private void processCourierDecline(Order order, User courierUser, Courier declinedCourier) {
        LocalDateTime declinedAt = LocalDateTime.now();
        recordCourierDecline(order, declinedCourier, declinedAt);
        assignReplacementOrDeclineOrder(order, courierUser, declinedCourier, declinedAt);
    }

    private void recordCourierDecline(Order order, Courier declinedCourier, LocalDateTime declinedAt) {
        courierAssignmentService.recordDecline(order, declinedCourier, declinedAt);
        courierAssignmentService.releaseCourier(declinedCourier);
    }

    private void assignReplacementOrDeclineOrder(Order order, User courierUser, Courier declinedCourier, LocalDateTime changedAt) {
        Optional<Courier> replacementAssignment = courierAssignmentService.tryAssignReplacementCourier(order);
        if (replacementAssignment.isPresent()) {
            assignReplacementCourier(order, courierUser, declinedCourier, changedAt, replacementAssignment.get());
            return;
        }

        declineOrderWithoutReplacement(order, courierUser, declinedCourier, changedAt);
    }

    private void assignReplacementCourier(Order order, User courierUser, Courier declinedCourier,
                                          LocalDateTime changedAt, Courier replacementCourier) {
        order.setCourier(replacementCourier);
        order.setUpdatedAt(changedAt);
        orderRepository.save(order);
        orderStatusHistoryService.saveOrderStatusHistory(order, OrderStatus.ACCEPTED, courierUser, changedAt,
                OrderWorkflowNotes.courierDeclinedAndReplacementAssigned(declinedCourier, replacementCourier));
        log.info("Courier {} declined order {}. Replacement courier {} assigned.",
                courierUser.getEmail(),
                order.getOrderNumber(),
                replacementCourier.getUser().getEmail());
    }

    private void declineOrderWithoutReplacement(Order order, User courierUser, Courier declinedCourier, LocalDateTime changedAt) {
        order.setCourier(null);
        changeOrderStatus(order, OrderStatus.DECLINED, courierUser, changedAt,
                OrderWorkflowNotes.courierDeclinedAndOrderDeclined(declinedCourier));
        log.info("Courier {} declined order {} and no replacement courier was available.",
                courierUser.getEmail(),
                order.getOrderNumber());
    }

    private void ensureMerchantCanAccept(Order order) {
        ensureMerchantOrderIsNotFinal(order);
        ensureMerchantStatus(order, OrderStatus.PENDING, ErrorMessages.ACCEPT_PENDING_ONLY);
    }

    private void ensureCourierCanConfirm(Order order) {
        ensureCourierOrderIsNotFinal(order);
        ensureCourierStatus(order, OrderStatus.ACCEPTED, ErrorMessages.COURIER_CONFIRM_ACCEPTED_ONLY);
    }

    private void ensureCourierCanDecline(Order order) {
        ensureCourierOrderIsNotFinal(order);
        ensureCourierStatus(order, OrderStatus.ACCEPTED, ErrorMessages.COURIER_DECLINE_ACCEPTED_ONLY);
    }

    private void ensureMerchantCanMarkPreparing(Order order) {
        ensureMerchantOrderIsNotFinal(order);
        ensureMerchantStatus(order, OrderStatus.COURIER_ACCEPTED, ErrorMessages.PREPARING_COURIER_ACCEPTED_ONLY);
    }

    private void ensureMerchantCanMarkPrepared(Order order) {
        ensureMerchantOrderIsNotFinal(order);
        ensureMerchantStatus(order, OrderStatus.PREPARING, ErrorMessages.PREPARED_PREPARING_ONLY);
    }

    private void ensureCourierCanMarkOnTheWay(Order order) {
        ensureCourierOrderIsNotFinal(order);
        ensureCourierStatus(order, OrderStatus.PREPARED, ErrorMessages.ON_THE_WAY_PREPARED_ONLY);
    }

    private void ensureCourierCanMarkDelivered(Order order) {
        ensureCourierOrderIsNotFinal(order);
        ensureCourierStatus(order, OrderStatus.ON_THE_WAY, ErrorMessages.DELIVERED_ON_THE_WAY_ONLY);
    }

    private void ensureMerchantOrderIsNotFinal(Order order) {
        if (OrderWorkflowRules.isFinal(order.getStatus())) {
            throw new MerchantOrderWorkflowException(ErrorMessages.FINAL_ORDER_CHANGE_DENIED);
        }
    }

    private void ensureCourierOrderIsNotFinal(Order order) {
        if (OrderWorkflowRules.isFinal(order.getStatus())) {
            throw new CourierOrderWorkflowException(ErrorMessages.FINAL_ORDER_CHANGE_DENIED);
        }
    }

    private void ensureMerchantStatus(Order order, OrderStatus expectedStatus, String message) {
        if (order.getStatus() != expectedStatus) {
            throw new MerchantOrderWorkflowException(message);
        }
    }

    private void ensureCourierStatus(Order order, OrderStatus expectedStatus, String message) {
        if (order.getStatus() != expectedStatus) {
            throw new CourierOrderWorkflowException(message);
        }
    }

    private void ensureMerchantCanCancel(Order order) {
        ensureMerchantOrderIsNotFinal(order);
        if (!OrderWorkflowRules.canMerchantCancel(order.getStatus())) {
            throw new MerchantOrderWorkflowException(ErrorMessages.CANCEL_ALLOWED_STATUSES_ONLY);
        }
    }

    private Courier releaseAssignedCourierIfPresent(Order order) {
        Courier courier = order.getCourier();
        if (courier == null) {
            return null;
        }

        return courierAssignmentService.releaseCourier(courier);
    }
}
