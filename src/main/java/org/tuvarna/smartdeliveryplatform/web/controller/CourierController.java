package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderWorkflowService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;
import org.tuvarna.smartdeliveryplatform.web.util.RedirectUrlResolver;

import java.util.List;

@Controller
@RequestMapping("/courier")
public class CourierController {
    private final CourierService courierService;
    private final OrderService orderService;
    private final OrderWorkflowService orderWorkflowService;
    private final UserService userService;
    private final RedirectUrlResolver redirectUrlResolver;

    public CourierController(CourierService courierService,
                             OrderService orderService,
                             OrderWorkflowService orderWorkflowService,
                             UserService userService,
                             RedirectUrlResolver redirectUrlResolver) {
        this.courierService = courierService;
        this.orderService = orderService;
        this.orderWorkflowService = orderWorkflowService;
        this.userService = userService;
        this.redirectUrlResolver = redirectUrlResolver;
    }

    @GetMapping("/orders")
    public String getCourierOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                   Model model) {
        List<OrderSummaryResponse> orders = orderService.getOrdersForCourier(authenticationMetadata.getUsername());

        model.addAttribute("orders", orders);
        return "courier/courier-orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String getCourierOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                         @PathVariable String orderNumber,
                                         Model model) {
        OrderDetailsResponse order = orderService.getOrderDetailsForCourier(orderNumber, authenticationMetadata.getUsername());

        model.addAttribute("order", order);
        return "order/details";
    }

    @PostMapping("/orders/{orderNumber}/confirm")
    public String confirmOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                               @PathVariable String orderNumber,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.confirmByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.DELIVERY_CONFIRMED);

        return "redirect:/courier/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/decline")
    public String declineOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                               @PathVariable String orderNumber,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.declineByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.DELIVERY_DECLINED);

        return "redirect:/courier/orders";
    }

    @PostMapping("/orders/{orderNumber}/on-the-way")
    public String markOrderOnTheWay(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable String orderNumber,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markOnTheWayByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_MARKED_ON_THE_WAY);

        return "redirect:/courier/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/delivered")
    public String markOrderDelivered(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String orderNumber,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markDeliveredByCourier(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_MARKED_DELIVERED);

        return "redirect:/courier/orders/" + orderNumber;
    }

    @PostMapping("/toggle-availability")
    public String toggleAvailability(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     HttpServletRequest request) {
        courierService.toggleCourierAvailability(authenticationMetadata.getUsername());
        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/courier/orders");
    }
}
