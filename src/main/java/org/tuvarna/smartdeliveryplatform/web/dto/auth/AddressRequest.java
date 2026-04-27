package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import lombok.*;

@Data
public class AddressRequest {
    private String city;
    private String street;
    private String building;
    private Boolean isDefault;
}