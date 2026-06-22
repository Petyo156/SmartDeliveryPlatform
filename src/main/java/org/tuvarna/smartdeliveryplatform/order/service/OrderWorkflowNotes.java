package org.tuvarna.smartdeliveryplatform.order.service;

import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.user.model.User;

final class OrderWorkflowNotes {
    static final String ORDER_NOT_FOUND = "Order not found.";
    static final String FINAL_ORDER_CHANGE_DENIED = "Finalized orders cannot be changed.";
    static final String ACCEPT_PENDING_ONLY = "Only pending orders can be accepted.";
    static final String CANCEL_ALLOWED_STATUSES_ONLY = "Only orders that have not started delivery can be cancelled.";
    static final String COURIER_CONFIRM_ACCEPTED_ONLY = "Only accepted orders can be confirmed.";
    static final String COURIER_DECLINE_ACCEPTED_ONLY = "Only accepted orders can be declined.";
    static final String PREPARING_COURIER_ACCEPTED_ONLY = "Only courier-confirmed orders can be marked as preparing.";
    static final String PREPARED_PREPARING_ONLY = "Only preparing orders can be marked as prepared.";
    static final String ON_THE_WAY_PREPARED_ONLY = "Only prepared orders can be marked as on the way.";
    static final String DELIVERED_ON_THE_WAY_ONLY = "Only orders on the way can be marked as delivered.";
    static final String ORDER_PLACED = "Order placed";
    static final String COURIER_ACCEPTED = "Courier confirmed the delivery.";
    static final String MERCHANT_PREPARING = "Merchant started preparing the order.";
    static final String MERCHANT_PREPARED = "Merchant marked the order as prepared.";
    static final String COURIER_ON_THE_WAY = "Courier picked up the order and is on the way.";

    private static final String MERCHANT_ACCEPTED_TEMPLATE = "Order accepted by merchant. Courier assigned: %s.";
    private static final String MERCHANT_CANCELLED = "Order cancelled by merchant.";
    private static final String COURIER_DECLINED_TEMPLATE = "Courier declined the delivery: %s.";
    private static final String REPLACEMENT_COURIER_ASSIGNED_TEMPLATE = "Replacement courier assigned: %s.";
    private static final String ORDER_DECLINED_NO_COURIER = "Order declined automatically because no replacement courier is available.";
    private static final String COURIER_DELIVERED = "Courier delivered the order.";
    private static final String COURIER_RELEASED_TEMPLATE = " Courier released: %s.";

    private OrderWorkflowNotes() {
    }

    static String merchantAccepted(Courier courier) {
        return MERCHANT_ACCEPTED_TEMPLATE.formatted(courierContact(courier));
    }

    static String merchantCancelled(Courier releasedCourier) {
        return MERCHANT_CANCELLED + releasedCourierNote(releasedCourier);
    }

    static String courierDeclinedAndReplacementAssigned(Courier declinedCourier, Courier replacementCourier) {
        return COURIER_DECLINED_TEMPLATE.formatted(courierContact(declinedCourier)) + " "
                + REPLACEMENT_COURIER_ASSIGNED_TEMPLATE.formatted(courierContact(replacementCourier));
    }

    static String courierDeclinedAndOrderDeclined(Courier declinedCourier) {
        return COURIER_DECLINED_TEMPLATE.formatted(courierContact(declinedCourier)) + " "
                + ORDER_DECLINED_NO_COURIER;
    }

    static String courierDelivered(Courier releasedCourier) {
        return COURIER_DELIVERED + releasedCourierNote(releasedCourier);
    }

    private static String releasedCourierNote(Courier courier) {
        if (courier == null) {
            return "";
        }

        return COURIER_RELEASED_TEMPLATE.formatted(courierContact(courier));
    }

    private static String courierContact(Courier courier) {
        User courierUser = courier.getUser();
        return "%s %s (%s)".formatted(
                courierUser.getFirstName(),
                courierUser.getLastName(),
                courierUser.getPhoneNumber()
        );
    }
}
