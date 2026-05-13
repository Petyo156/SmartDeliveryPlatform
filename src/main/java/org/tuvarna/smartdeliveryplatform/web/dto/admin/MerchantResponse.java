package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantResponse {
    private String ownerEmail;

    private String name;

    private String description;

    private MerchantType type;

    private Address address;

    private Boolean isActive;

    private Boolean isClosed;

    private LocalDateTime createdAt;

    private String imageUrl;
}
