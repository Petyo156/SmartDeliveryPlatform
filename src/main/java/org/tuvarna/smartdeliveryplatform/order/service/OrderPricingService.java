package org.tuvarna.smartdeliveryplatform.order.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderPricingService {
    public static final BigDecimal DEFAULT_DELIVERY_FEE = BigDecimal.TWO;
    public static final BigDecimal MINIMUM_ORDER_AMOUNT = BigDecimal.valueOf(5);

    public BigDecimal calculateTotal(BigDecimal subtotal) {
        return subtotal.add(DEFAULT_DELIVERY_FEE);
    }
}
