package org.tuvarna.smartdeliveryplatform.web.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.web.validation.ValidOrderPlacement;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidOrderPlacement
public class OrderPlacementRequest {
    @NotNull(message = ValidationMessages.DELIVERY_ADDRESS_OPTION_REQUIRED)
    private CheckoutAddressMode addressMode;

    private UUID addressId;

    private String city;

    private String street;

    private String building;
}
