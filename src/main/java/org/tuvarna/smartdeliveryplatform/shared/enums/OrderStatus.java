package org.tuvarna.smartdeliveryplatform.shared.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public enum OrderStatus {
    PENDING(false),
    ACCEPTED(true),
    COURIER_ACCEPTED(true),
    PREPARING(true),
    PREPARED(true),
    ON_THE_WAY(true),
    DELIVERED(false),
    CANCELLED(false),
    DECLINED(false);

    private final boolean activeAssignedStatus;

    OrderStatus(boolean activeAssignedStatus) {
        this.activeAssignedStatus = activeAssignedStatus;
    }

    public static Set<OrderStatus> activeAssignedStatuses() {
        return Stream.of(values())
                .filter(OrderStatus::isActiveAssignedStatus)
                .collect(() -> EnumSet.noneOf(OrderStatus.class), EnumSet::add, EnumSet::addAll);
    }
}
