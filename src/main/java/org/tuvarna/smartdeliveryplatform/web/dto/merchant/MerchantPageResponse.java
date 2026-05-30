package org.tuvarna.smartdeliveryplatform.web.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPageResponse {
    private String slug;

    private String name;

    private String description;

    private String imageUrl;

    private MerchantType type;

    private Boolean isClosed;
}
