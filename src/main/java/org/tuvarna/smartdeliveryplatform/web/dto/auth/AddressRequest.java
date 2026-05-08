package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Street is required.")
    private String street;

    @NotBlank(message = "Building is required.")
    private String building;

    private boolean isDefault;
}