package org.tuvarna.smartdeliveryplatform.order.service;

import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;

final class OrderWorkflowNotes {
    static String merchantAccepted(Courier courier) {
        return SuccessMessages.MERCHANT_ACCEPTED_HISTORY_TEMPLATE.formatted(courierContact(courier));
    }

    static String merchantCancelled(Courier releasedCourier) {
        return SuccessMessages.MERCHANT_CANCELLED_HISTORY_NOTE + releasedCourierNote(releasedCourier);
    }

    static String courierDeclinedAndReplacementAssigned(Courier declinedCourier, Courier replacementCourier) {
        return SuccessMessages.COURIER_DECLINED_HISTORY_TEMPLATE.formatted(courierContact(declinedCourier)) + " "
                + SuccessMessages.REPLACEMENT_COURIER_ASSIGNED_HISTORY_TEMPLATE.formatted(courierContact(replacementCourier));
    }

    static String courierDeclinedAndOrderDeclined(Courier declinedCourier) {
        return SuccessMessages.COURIER_DECLINED_HISTORY_TEMPLATE.formatted(courierContact(declinedCourier)) + " "
                + SuccessMessages.ORDER_DECLINED_NO_COURIER_HISTORY_NOTE;
    }

    static String courierDelivered(Courier releasedCourier) {
        return SuccessMessages.COURIER_DELIVERED_HISTORY_NOTE + releasedCourierNote(releasedCourier);
    }

    private static String releasedCourierNote(Courier courier) {
        if (courier == null) {
            return "";
        }

        return SuccessMessages.COURIER_RELEASED_HISTORY_TEMPLATE.formatted(courierContact(courier));
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
