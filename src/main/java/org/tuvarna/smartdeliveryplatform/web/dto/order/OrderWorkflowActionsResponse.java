package org.tuvarna.smartdeliveryplatform.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderWorkflowActionsResponse {
    private boolean canAcceptByMerchant;

    private boolean canCancelByMerchant;

    private boolean canMarkPreparingByMerchant;

    private boolean canMarkPreparedByMerchant;

    private boolean canConfirmByCourier;

    private boolean canDeclineByCourier;

    private boolean canMarkOnTheWayByCourier;

    private boolean canMarkDeliveredByCourier;
}
