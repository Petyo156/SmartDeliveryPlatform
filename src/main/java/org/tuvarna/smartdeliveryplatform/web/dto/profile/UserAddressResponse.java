package org.tuvarna.smartdeliveryplatform.web.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddressResponse {
    private String id;

    private String city;

    private String street;

    private String building;

    private boolean isDefault;
}
