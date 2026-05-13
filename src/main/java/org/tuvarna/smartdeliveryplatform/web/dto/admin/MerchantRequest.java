package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantRequest {
    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Name is required.")
    private String name;

    private String description;

    @NotNull(message = "Merchant type is required.")
    private MerchantType type;

    @Valid
    @NotNull(message = "Address is required.")
    private AddressRequest address;

    @NotBlank(message = "Image url is required")
    private String imageUrl;
}