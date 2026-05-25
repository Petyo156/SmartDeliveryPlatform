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

import java.util.List;

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
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
            @RequestParam(required = false) String editAddressId) {
        ModelAndView modelAndView = new ModelAndView("profile/addresses");

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        List<UserAddressResponse> addresses = addressService.getAllAddressResponsesForUser(user);
        AddressRequest addressRequest = AddressRequest.builder().build();
        boolean canAddMoreAddresses = addressService.canAddMoreAddresses(user);

        if (editAddressId != null) {
            UserAddressResponse addressResponse = addressService.getAddressResponse(editAddressId, user);
            addressRequest = addressService.initializeAddressEditRequest(addressResponse);
            modelAndView.addObject("editAddressId", editAddressId);
        }

        modelAndView.addObject("user", user);
        modelAndView.addObject("addresses", addresses);
        modelAndView.addObject("addressRequest", addressRequest);
        modelAndView.addObject("canAddMoreAddresses", canAddMoreAddresses);

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView createAddress(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @Valid @ModelAttribute("addressRequest") AddressRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile/addresses");
            User user = userService.getAuthenticatedUser(authenticationMetadata);
            List<UserAddressResponse> addresses = addressService.getAllAddressResponsesForUser(user);
            modelAndView.addObject("user", user);
            modelAndView.addObject("addresses", addresses);
            modelAndView.addObject("addressRequest", request);
            modelAndView.addObject("canAddMoreAddresses", addressService.canAddMoreAddresses(user));
            return modelAndView;
        }

        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
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
        List<UserAddressResponse> addresses = addressService.getAllAddressResponsesForUser(user);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile/addresses");
            modelAndView.addObject("user", user);
            modelAndView.addObject("addresses", addresses);
            modelAndView.addObject("addressRequest", addressRequest);
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
}