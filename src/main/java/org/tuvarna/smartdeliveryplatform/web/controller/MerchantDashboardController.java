package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderWorkflowService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;
import org.tuvarna.smartdeliveryplatform.web.util.RedirectUrlResolver;
import java.util.List;

@Controller
@RequestMapping("/dashboard/merchant")
public class MerchantDashboardController {
    private final MerchantService merchantService;
    private final OrderService orderService;
    private final OrderWorkflowService orderWorkflowService;
    private final AddressService addressService;
    private final UserService userService;
    private final RedirectUrlResolver redirectUrlResolver;

    public MerchantDashboardController(MerchantService merchantService,
                                       OrderService orderService,
                                       OrderWorkflowService orderWorkflowService,
                                       AddressService addressService,
                                       UserService userService,
                                       RedirectUrlResolver redirectUrlResolver) {
        this.merchantService = merchantService;
        this.orderService = orderService;
        this.orderWorkflowService = orderWorkflowService;
        this.addressService = addressService;
        this.userService = userService;
        this.redirectUrlResolver = redirectUrlResolver;
    }

    @GetMapping("/my-shop")
    public ModelAndView getMyShopPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        MerchantProfileRequest merchantProfileRequest = merchantService.getMerchantProfileRequest(authenticationMetadata.getUsername());
        return initializeMyShopPage(authenticationMetadata, merchantProfileRequest);
    }

    private ModelAndView initializeMyShopPage(AuthenticationMetadata authenticationMetadata,
                                             MerchantProfileRequest merchantProfileRequest) {
        ModelAndView modelAndView = new ModelAndView("merchant/merchant");

        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        List<MerchantAddressResponse> merchantAddressResponses = addressService.getAllAddressesForMerchant(user);

        modelAndView.addObject("merchantProfileRequest", merchantProfileRequest);
        modelAndView.addObject("merchantAddressResponses", merchantAddressResponses);

        return modelAndView;
    }

    @PostMapping("/my-shop")
    public ModelAndView updateMyShop(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                               @Valid @ModelAttribute("merchantProfileRequest") MerchantProfileRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return initializeMyShopPage(authenticationMetadata, request);
        }

        merchantService.updateMerchantProfile(authenticationMetadata, request);
        redirectAttributes.addFlashAttribute("successMessage", "Merchant profile updated successfully!");
        return new ModelAndView("redirect:/dashboard/merchant/my-shop");
    }

    @GetMapping("/orders")
    public ModelAndView getMerchantOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/orders");
        List<OrderSummaryResponse> orders = orderService.getOrdersForMerchant(authenticationMetadata.getUsername());

        modelAndView.addObject("orders", orders);

        return modelAndView;
    }

    @GetMapping("/orders/{orderNumber}")
    public ModelAndView getMerchantOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                                @PathVariable String orderNumber) {
        ModelAndView modelAndView = new ModelAndView("order/details");
        OrderDetailsResponse order = orderService.getOrderDetailsForMerchant(orderNumber, authenticationMetadata.getUsername());

        modelAndView.addObject("order", order);

        return modelAndView;
    }

    @PostMapping("/orders/{orderNumber}/accept")
    public ModelAndView acceptOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable String orderNumber,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.acceptByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order accepted and courier assigned.");

        return new ModelAndView("redirect:/dashboard/merchant/orders/" + orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/cancel")
    public ModelAndView cancelOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable String orderNumber,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.cancelByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order cancelled.");

        return new ModelAndView("redirect:/dashboard/merchant/orders/" + orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/preparing")
    public ModelAndView markOrderPreparing(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                           @PathVariable String orderNumber,
                                           RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markPreparingByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as preparing.");

        return new ModelAndView("redirect:/dashboard/merchant/orders/" + orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/prepared")
    public ModelAndView markOrderPrepared(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                          @PathVariable String orderNumber,
                                          RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markPreparedByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", "Order marked as prepared.");

        return new ModelAndView("redirect:/dashboard/merchant/orders/" + orderNumber);
    }

    @PostMapping("/toggle-closed-status")
    public String toggleShopStatus(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, HttpServletRequest request) {
        merchantService.toggleMerchantIsClosedStatus(authenticationMetadata.getUsername());
        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/dashboard/merchant/my-shop");
    }
}
