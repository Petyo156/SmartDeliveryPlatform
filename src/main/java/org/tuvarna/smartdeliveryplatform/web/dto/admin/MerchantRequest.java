package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantRequest {
    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    private String email;

    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    private String name;

    private String description;

    @NotNull(message = ValidationMessages.MERCHANT_TYPE_REQUIRED)
    private MerchantType type;

    @Valid
    @NotNull(message = ValidationMessages.ADDRESS_REQUIRED_PERIOD)
    private AddressRequest address;

    @NotBlank(message = ValidationMessages.IMAGE_URL_REQUIRED)
    private String imageUrl;
}
