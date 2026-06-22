package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderWorkflowService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;

import java.util.List;

@Controller
@RequestMapping("/courier")
public class CourierController {
    private final OrderService orderService;
    private final OrderWorkflowService orderWorkflowService;
    private final UserService userService;

    public CourierController(OrderService orderService,
                             OrderWorkflowService orderWorkflowService,
                             UserService userService) {
        this.orderService = orderService;
        this.orderWorkflowService = orderWorkflowService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public ModelAndView getCourierOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        List<OrderSummaryResponse> orders = orderService.getOrdersForCourier(authenticationMetadata.getUsername());

        ModelAndView modelAndView = new ModelAndView("courier/courier-orders");
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }

    @GetMapping("/orders/{orderNumber}")
    public ModelAndView getCourierOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                               @PathVariable String orderNumber) {
        OrderDetailsResponse order = orderService.getOrderDetailsForCourier(orderNumber, authenticationMetadata.getUsername());

        ModelAndView modelAndView = new ModelAndView("order/details");
        modelAndView.addObject("order", order);
        return modelAndView;
    }

    @PostMapping("/orders/{orderNumber}/confirm")
    public ModelAndView confirmOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String orderNumber,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.confirmByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery confirmed.");

        return new ModelAndView("redirect:/courier/orders/" + orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/decline")
    public ModelAndView declineOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String orderNumber,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.declineByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery declined.");

        return new ModelAndView("redirect:/courier/orders");
    }

    @PostMapping("/orders/{orderNumber}/on-the-way")
    public ModelAndView markOrderOnTheWay(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                          @PathVariable String orderNumber,
                                          RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markOnTheWayByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as on the way.");

        return new ModelAndView("redirect:/courier/orders/" + orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/delivered")
    public ModelAndView markOrderDelivered(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                           @PathVariable String orderNumber,
                                           RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markDeliveredByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as delivered.");

        return new ModelAndView("redirect:/courier/orders/" + orderNumber);
    }
}
