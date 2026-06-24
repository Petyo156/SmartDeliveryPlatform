package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/addresses")
public class AddressController {
    private final AddressService addressService;
    private final UserService userService;
    private final FlashValidationAttributes flashValidationAttributes;

    public AddressController(AddressService addressService,
                             UserService userService,
                             FlashValidationAttributes flashValidationAttributes) {
        this.addressService = addressService;
        this.userService = userService;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping
    public String getAddressesPage(
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
            Model model) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        AddressRequest addressRequest = AddressRequest.builder().build();

        model.addAttribute("addresses", addressService.getAllAddressResponsesForUser(user));
        flashValidationAttributes.addModelAttributeIfMissing(model, "addressRequest", addressRequest);
        model.addAttribute("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
        model.addAttribute("editAddressId", null);

        return "profile/addresses";
    }

    @GetMapping("/{editAddressId}/edit")
    public String getEditAddressPage(
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
            @PathVariable UUID editAddressId,
            Model model) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        UserAddressResponse addressResponse = addressService.getAddressResponse(editAddressId, user);
        AddressRequest addressRequest = addressService.initializeAddressEditRequest(addressResponse);

        model.addAttribute("addresses", addressService.getAllAddressResponsesForUser(user));
        flashValidationAttributes.addModelAttributeIfMissing(model, "addressRequest", addressRequest);
        model.addAttribute("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
        model.addAttribute("editAddressId", editAddressId.toString());

        return "profile/addresses";
    }

    @PostMapping
    public String createAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @Valid @ModelAttribute("addressRequest") AddressRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "addressRequest", request, bindingResult);
            return "redirect:/addresses";
        }

        addressService.addAddress(user, request);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ADDRESS_ADDED);

        return "redirect:/addresses";
    }

    @PostMapping("/{editingAddressId}/edit")
    public String updateAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable UUID editingAddressId,
                                @Valid @ModelAttribute("addressRequest") AddressRequest addressRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "addressRequest", addressRequest, bindingResult);
            return "redirect:/addresses/" + editingAddressId + "/edit";
        }

        addressService.updateAddress(user, editingAddressId, addressRequest);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ADDRESS_UPDATED);

        return "redirect:/addresses";
    }

    @PostMapping("/{id}")
    public String deleteAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable UUID id,
                                RedirectAttributes redirectAttributes) {

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        addressService.deleteAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.ADDRESS_DELETED);
        return "redirect:/addresses";
    }

    @PostMapping("/{id}/default")
    public String setDefaultAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable UUID id,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        addressService.setDefaultAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.DEFAULT_ADDRESS_UPDATED);
        return "redirect:/addresses";
    }
}
