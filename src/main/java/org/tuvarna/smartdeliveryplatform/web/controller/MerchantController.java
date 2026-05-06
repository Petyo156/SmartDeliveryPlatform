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
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/merchant")
public class MerchantController {
    private final MerchantService merchantService;
    private final AddressService addressService;
    private final UserService userService;

    public MerchantController(MerchantService merchantService, AddressService addressService, UserService userService) {
        this.merchantService = merchantService;
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping("/my-shop")
    public ModelAndView getMyShopPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/merchant");

        User user = userService.getUserByEmail(authenticationMetadata.getUsername());

        List<MerchantAddressResponse> addresses = addressService.getAllAddressesForMerchant(user);
        Boolean merchantIsClosed = merchantService.merchantIsClosedStatus(authenticationMetadata);
        MerchantProfileRequest request = merchantService.getMerchantProfileRequest(user.getEmail());

        modelAndView.addObject("merchantProfile", request);
        modelAndView.addObject("addresses", addresses);
        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantIsClosed", merchantIsClosed);

        return modelAndView;
    }

    @PostMapping("/my-shop")
    public ModelAndView updateMyShop(@AuthenticationPrincipal AuthenticationMetadata principal,
                               @Valid @ModelAttribute("merchantProfile") MerchantProfileRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("redirect:/merchant/merchant");
        }

        Merchant merchant = merchantService.getMerchantEntityByEmail(principal.getUsername());
        merchantService.updateMerchantProfile(merchant, request);
        redirectAttributes.addFlashAttribute("successMessage", "Merchant profile updated successfully!");
        return new ModelAndView("redirect:/merchant/my-shop");
    }

    @GetMapping("/products")
    public ModelAndView getMerchantProducts(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/products");
        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        Boolean merchantIsClosed = merchantService.merchantIsClosedStatus(authenticationMetadata);

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantIsClosed", merchantIsClosed);

        return modelAndView;
    }

    @GetMapping("/orders")
    public ModelAndView getMerchantOrders(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/orders");
        User user = userService.getUserByEmail(authenticationMetadata.getUsername());

        Boolean merchantIsClosed = merchantService.merchantIsClosedStatus(authenticationMetadata);
        modelAndView.addObject("merchantIsClosed", merchantIsClosed);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/toggle-closed-status")
    public String toggleShopStatus(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, HttpServletRequest request) {
        merchantService.toggleMerchantIsClosedStatus(authenticationMetadata.getUsername());
        String referer = request.getHeader("Referer");

        if (referer != null) {
            URI uri = URI.create(referer);
            if (uri.getPath() != null && uri.getPath().startsWith("/")) {
                return "redirect:" + uri.getPath();
            }
        }

        return "redirect:/";
    }
}
