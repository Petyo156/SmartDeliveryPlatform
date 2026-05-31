package org.tuvarna.smartdeliveryplatform.web.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String slug;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private Boolean isAvailable;

    private String imageUrl;

    private String categoryName;
}