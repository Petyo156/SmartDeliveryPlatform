package org.tuvarna.smartdeliveryplatform.web.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MerchantAddressResponse {
    private UUID id;

    private String city;

    private String street;

    private String building;
}
