package org.tuvarna.smartdeliveryplatform.web.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacementRequest {
    @NotNull(message = "Delivery address option is required.")
    private CheckoutAddressMode addressMode;

    private UUID addressId;

    private String city;

    private String street;

    private String building;
}
