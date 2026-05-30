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
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;
import java.util.List;

@Controller
@RequestMapping("/dashboard/merchant")
public class MerchantDashboardController {
    private final MerchantService merchantService;
    private final AddressService addressService;
    private final UserService userService;

    public MerchantDashboardController(MerchantService merchantService, AddressService addressService, UserService userService) {
        this.merchantService = merchantService;
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping("/my-shop")
    public ModelAndView getMyShopPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/merchant");

        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        List<MerchantAddressResponse> merchantAddressResponses = addressService.getAllAddressesForMerchant(user);
        MerchantProfileRequest merchantProfileRequest = merchantService.getMerchantProfileRequest(user.getEmail());

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantProfileRequest", merchantProfileRequest);
        modelAndView.addObject("merchantAddressResponses", merchantAddressResponses);

        return modelAndView;
    }

    @PostMapping("/my-shop")
    public ModelAndView updateMyShop(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                               @Valid @ModelAttribute("merchantProfile") MerchantProfileRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("redirect:/dashboard/merchant/my-shop");
        }

        merchantService.updateMerchantProfile(authenticationMetadata, request);
        redirectAttributes.addFlashAttribute("successMessage", "Merchant profile updated successfully!");
        return new ModelAndView("redirect:/dashboard/merchant/my-shop");
    }

    @GetMapping("/orders")
    public ModelAndView getMerchantOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/orders");
        User user = userService.getUserByEmail(authenticationMetadata.getUsername());

        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/toggle-closed-status")
    public String toggleShopStatus(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, HttpServletRequest request) {
        merchantService.toggleMerchantIsClosedStatus(authenticationMetadata.getUsername());
        return "redirect:" + request.getHeader("Referer");
    }
}
