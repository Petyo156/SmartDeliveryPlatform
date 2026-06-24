package org.tuvarna.smartdeliveryplatform.web.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantProfileRequest {

    @NotBlank(message = ValidationMessages.MERCHANT_NAME_REQUIRED)
    private String name;

    private String description;

    @NotNull(message = ValidationMessages.ADDRESS_REQUIRED)
    private UUID addressId;

    private Boolean isClosed;

    @NotBlank(message = ValidationMessages.IMAGE_URL_REQUIRED)
    private String imageUrl;
}
