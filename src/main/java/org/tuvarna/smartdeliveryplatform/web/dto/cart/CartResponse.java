package org.tuvarna.smartdeliveryplatform.web.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;

    private String merchantName;

    private List<CartItemResponse> items;

    private BigDecimal subtotal;

    private BigDecimal deliveryFee;

    private BigDecimal total;

    private Boolean empty;

    private Boolean checkoutAvailable;

    private String checkoutUnavailableMessage;
}
