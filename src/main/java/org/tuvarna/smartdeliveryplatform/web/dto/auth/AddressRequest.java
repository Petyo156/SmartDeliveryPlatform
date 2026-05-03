package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private String city;
    private String street;
    private String building;
    private boolean isDefault;
}