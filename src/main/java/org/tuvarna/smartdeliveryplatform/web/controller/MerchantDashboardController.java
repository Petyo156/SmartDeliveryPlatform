package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderWorkflowService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderDetailsResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderSummaryResponse;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;
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
    private final FlashValidationAttributes flashValidationAttributes;

    public MerchantDashboardController(MerchantService merchantService,
                                       OrderService orderService,
                                       OrderWorkflowService orderWorkflowService,
                                       AddressService addressService,
                                       UserService userService,
                                       RedirectUrlResolver redirectUrlResolver,
                                       FlashValidationAttributes flashValidationAttributes) {
        this.merchantService = merchantService;
        this.orderService = orderService;
        this.orderWorkflowService = orderWorkflowService;
        this.addressService = addressService;
        this.userService = userService;
        this.redirectUrlResolver = redirectUrlResolver;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping("/my-shop")
    public String getMyShopPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                Model model) {
        MerchantProfileRequest merchantProfileRequest = merchantService.getMerchantProfileRequest(authenticationMetadata.getUsername());
        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        List<MerchantAddressResponse> merchantAddressResponses = addressService.getAllAddressesForMerchant(user);

        flashValidationAttributes.addModelAttributeIfMissing(model, "merchantProfileRequest", merchantProfileRequest);
        model.addAttribute("merchantAddressResponses", merchantAddressResponses);

        return "merchant/merchant";
    }

    @PostMapping("/my-shop")
    public String updateMyShop(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                               @Valid @ModelAttribute("merchantProfileRequest") MerchantProfileRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "merchantProfileRequest", request, bindingResult);
            return "redirect:/dashboard/merchant/my-shop";
        }

        merchantService.updateMerchantProfile(authenticationMetadata, request);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.MERCHANT_PROFILE_UPDATED);
        return "redirect:/dashboard/merchant/my-shop";
    }

    @GetMapping("/orders")
    public String getMerchantOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    Model model) {
        List<OrderSummaryResponse> orders = orderService.getOrdersForMerchant(authenticationMetadata.getUsername());

        model.addAttribute("orders", orders);

        return "merchant/orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String getMerchantOrderDetails(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                          @PathVariable String orderNumber,
                                          Model model) {
        OrderDetailsResponse order = orderService.getOrderDetailsForMerchant(orderNumber, authenticationMetadata.getUsername());

        model.addAttribute("order", order);

        return "order/details";
    }

    @PostMapping("/orders/{orderNumber}/accept")
    public String acceptOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                              @PathVariable String orderNumber,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.acceptByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_ACCEPTED_AND_COURIER_ASSIGNED);

        return "redirect:/dashboard/merchant/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/cancel")
    public String cancelOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                              @PathVariable String orderNumber,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.cancelByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_CANCELLED);

        return "redirect:/dashboard/merchant/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/preparing")
    public String markOrderPreparing(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String orderNumber,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markPreparingByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_MARKED_PREPARING);

        return "redirect:/dashboard/merchant/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/prepared")
    public String markOrderPrepared(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable String orderNumber,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderWorkflowService.markPreparedByMerchant(orderNumber, user);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ORDER_MARKED_PREPARED);

        return "redirect:/dashboard/merchant/orders/" + orderNumber;
    }

    @PostMapping("/toggle-closed-status")
    public String toggleShopStatus(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, HttpServletRequest request) {
        merchantService.toggleMerchantIsClosedStatus(authenticationMetadata.getUsername());
        return "redirect:" + redirectUrlResolver.resolveRefererOrDefault(request, "/dashboard/merchant/my-shop");
    }
}
