package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView getMyOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        List<OrderSummaryResponse> orders = orderService.getOrdersForUser(authenticationMetadata.getUsername());

        ModelAndView modelAndView = new ModelAndView("order/orders");
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }

    @GetMapping("/{orderNumber}")
    public ModelAndView getOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                        @PathVariable String orderNumber) {
        OrderDetailsResponse order = orderService.getOrderDetailsForUser(orderNumber, authenticationMetadata.getUsername());

        ModelAndView modelAndView = new ModelAndView("order/details");
        modelAndView.addObject("order", order);
        return modelAndView;
    }
}
