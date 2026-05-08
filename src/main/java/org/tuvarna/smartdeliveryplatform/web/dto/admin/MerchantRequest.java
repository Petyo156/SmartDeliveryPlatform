package org.tuvarna.smartdeliveryplatform.web.dto.admin;

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
    private String email;
    private String name;
    private String description;
    private MerchantType type;
    private AddressRequest address;
}