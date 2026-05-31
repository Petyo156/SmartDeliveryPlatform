package org.tuvarna.smartdeliveryplatform.web.dto.cart;

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
public class CartItemResponse {
    private UUID id;

    private String productSlug;

    private String productName;

    private String productImageUrl;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineSubtotal;
}
