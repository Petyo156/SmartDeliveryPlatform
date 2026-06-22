package org.tuvarna.smartdeliveryplatform.order.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderPricingService {
    public static final BigDecimal DEFAULT_DELIVERY_FEE = BigDecimal.TWO;

    public BigDecimal calculateTotal(BigDecimal subtotal) {
        return subtotal.add(DEFAULT_DELIVERY_FEE);
    }
}
