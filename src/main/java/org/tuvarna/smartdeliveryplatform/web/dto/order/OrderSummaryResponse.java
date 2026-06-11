package org.tuvarna.smartdeliveryplatform.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.OrderStatus;
import org.tuvarna.smartdeliveryplatform.shared.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private UUID id;

    private String orderNumber;

    private String merchantName;

    private String merchantAddress;

    private String clientName;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    private BigDecimal subtotal;

    private BigDecimal deliveryFee;

    private BigDecimal totalPrice;

    private String deliveryAddress;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}
