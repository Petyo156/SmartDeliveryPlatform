package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierResponse {
    private String userEmail;

    private Boolean isActive;

    private Boolean isAvailable;
}
