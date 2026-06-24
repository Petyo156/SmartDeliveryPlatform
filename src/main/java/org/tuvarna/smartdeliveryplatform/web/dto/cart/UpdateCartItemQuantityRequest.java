package org.tuvarna.smartdeliveryplatform.web.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemQuantityRequest {
    @NotNull(message = ValidationMessages.QUANTITY_REQUIRED)
    @Min(value = 1, message = ValidationMessages.QUANTITY_MIN_ONE)
    private Integer quantity;
}
