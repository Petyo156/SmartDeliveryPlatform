package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    @NotBlank(message = ValidationMessages.CITY_REQUIRED)
    private String city;

    @NotBlank(message = ValidationMessages.STREET_REQUIRED)
    private String street;

    @NotBlank(message = ValidationMessages.BUILDING_REQUIRED)
    private String building;

    private boolean isDefault;
}
