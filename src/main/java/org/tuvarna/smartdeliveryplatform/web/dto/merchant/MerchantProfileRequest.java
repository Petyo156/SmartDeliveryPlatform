package org.tuvarna.smartdeliveryplatform.web.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantProfileRequest {

    @NotBlank(message = "Merchant name is required")
    private String name;

    private String description;

    @NotNull(message = "Address is required")
    private UUID addressId;

    private Boolean isClosed;

    @NotBlank(message = "Image url is required")
    private String imageUrl;
}