package org.tuvarna.smartdeliveryplatform.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private String productName;

    private String productImageUrl;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineSubtotal;
}
