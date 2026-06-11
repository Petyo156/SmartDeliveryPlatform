package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;

@Controller
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getAddressesPage(
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("profile/addresses");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        AddressRequest addressRequest = AddressRequest.builder().build();

        modelAndView.addObject("addresses", addressService.getAllAddressResponsesForUser(user));
        modelAndView.addObject("addressRequest", addressRequest);
        modelAndView.addObject("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
        modelAndView.addObject("editAddressId", null);

        return modelAndView;
    }

    @GetMapping("/{editAddressId}/edit")
    public ModelAndView getEditAddressPage(
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
            @PathVariable String editAddressId) {
        ModelAndView modelAndView = new ModelAndView("profile/addresses");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        UserAddressResponse addressResponse = addressService.getAddressResponse(editAddressId, user);
        AddressRequest addressRequest = addressService.initializeAddressEditRequest(addressResponse);

        modelAndView.addObject("addresses", addressService.getAllAddressResponsesForUser(user));
        modelAndView.addObject("addressRequest", addressRequest);
        modelAndView.addObject("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
        modelAndView.addObject("editAddressId", editAddressId);

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView createAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @Valid @ModelAttribute("addressRequest") AddressRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile/addresses");

            modelAndView.addObject("addresses", addressService.getAllAddressResponsesForUser(user));
            modelAndView.addObject("addressRequest", request);
            modelAndView.addObject("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
            modelAndView.addObject("editAddressId", null);

            return modelAndView;
        }

        addressService.addAddress(user, request);
        redirectAttributes.addFlashAttribute("successMessage", "Address added successfully!");

        return new ModelAndView("redirect:/addresses");
    }

    @PostMapping("/{editingAddressId}/edit")
    public ModelAndView updateAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @PathVariable String editingAddressId,
                                      @Valid @ModelAttribute("addressRequest") AddressRequest addressRequest,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile/addresses");

            modelAndView.addObject("addresses", addressService.getAllAddressResponsesForUser(user));
            modelAndView.addObject("addressRequest", addressRequest);
            modelAndView.addObject("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
            modelAndView.addObject("editAddressId", editingAddressId);

            return modelAndView;
        }

        addressService.updateAddress(user, editingAddressId, addressRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Address updated successfully!");

        return new ModelAndView("redirect:/addresses");
    }

    @PostMapping("/{id}")
    public String deleteAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable String id,
                                RedirectAttributes redirectAttributes) {

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        addressService.deleteAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", "Address deleted!");
        return "redirect:/addresses";
    }

    @PostMapping("/{id}/default")
    public String setDefaultAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @PathVariable String id,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        addressService.setDefaultAddress(user, id);
        redirectAttributes.addFlashAttribute("successMessage", "Default address updated!");
        return "redirect:/addresses";
    }
}
