package org.tuvarna.smartdeliveryplatform.web.dto.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemoMerchantRequest {
    private String email;

    private String name;

    private String description;

    private MerchantType type;

    private String city;

    private String street;

    private String building;

    private String imageUrl;

}
