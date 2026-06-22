package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String getMyOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                              Model model) {
        List<OrderSummaryResponse> orders = orderService.getOrdersForUser(authenticationMetadata.getUsername());

        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @GetMapping("/{orderNumber}")
    public String getOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                  @PathVariable String orderNumber,
                                  Model model) {
        OrderDetailsResponse order = orderService.getOrderDetailsForUser(orderNumber, authenticationMetadata.getUsername());

        model.addAttribute("order", order);
        return "order/details";
    }
}
