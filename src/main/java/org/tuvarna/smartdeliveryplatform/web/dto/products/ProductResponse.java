package org.tuvarna.smartdeliveryplatform.web.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private Boolean isAvailable;

    private String imageUrl;

    private String categoryName;
}