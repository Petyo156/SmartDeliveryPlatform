package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import lombok.Data;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;

@Data
public class MerchantRequest {
    private String email;
    private String name;
    private String description;
    private MerchantType type;
    private AddressRequest address;
}