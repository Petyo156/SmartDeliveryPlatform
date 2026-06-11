package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.AddCartItemRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.CartResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.UpdateCartItemQuantityRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.util.RedirectUrlResolver;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final UserService userService;
    private final RedirectUrlResolver redirectUrlResolver;

    public CartController(CartService cartService,
                          OrderService orderService,
                          AddressService addressService,
                          UserService userService,
                          RedirectUrlResolver redirectUrlResolver) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.userService = userService;
        this.redirectUrlResolver = redirectUrlResolver;
    }

    @GetMapping
    public ModelAndView getCart(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        CartResponse cart = cartService.getCartResponse(user);
        List<UserAddressResponse> addresses = addressService.getAllAddressResponsesForUser(user);
        boolean canAddMoreAddresses = addressService.canAddMoreAddresses(user);

        ModelAndView modelAndView = new ModelAndView("cart/cart");
        modelAndView.addObject("cart", cart);
        modelAndView.addObject("addresses", addresses);
        modelAndView.addObject("canAddMoreAddresses", canAddMoreAddresses);
        modelAndView.addObject("quantityRequest", UpdateCartItemQuantityRequest.builder().build());
        modelAndView.addObject("orderPlacementRequest", orderService.initializeOrderPlacementRequest(addresses));
        return modelAndView;
    }

    @PostMapping("/items")
    public ModelAndView addItem(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @Valid @ModelAttribute AddCartItemRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest httpServletRequest) {
        String redirectUrl = redirectUrlResolver.resolveRefererOrDefault(httpServletRequest, "/cart");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose a valid product quantity.");
            return new ModelAndView("redirect:" + redirectUrl);
        }

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        cartService.addProductToCart(user, request);
        redirectAttributes.addFlashAttribute("successMessage", "Product added to cart.");
        return new ModelAndView("redirect:" + redirectUrl);
    }

    @PostMapping("/items/clear-and-add")
    public ModelAndView clearCartAndAddItem(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                            @Valid @ModelAttribute AddCartItemRequest request,
                                            BindingResult bindingResult,
                                            RedirectAttributes redirectAttributes,
                                            HttpServletRequest httpServletRequest) {
        String redirectUrl = redirectUrlResolver.resolveRefererOrDefault(httpServletRequest, "/cart");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose a valid product quantity.");
            return new ModelAndView("redirect:" + redirectUrl);
        }

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        cartService.clearCartAndAddProduct(user, request);
        redirectAttributes.addFlashAttribute("successMessage", "Cart cleared and product added.");
        return new ModelAndView("redirect:" + redirectUrl);
    }

    @PostMapping("/items/{itemId}/quantity")
    public ModelAndView updateQuantity(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                       @PathVariable UUID itemId,
                                       @Valid @ModelAttribute UpdateCartItemQuantityRequest request,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity must be at least 1.");
            return new ModelAndView("redirect:/cart");
        }

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        cartService.updateItemQuantity(user, itemId, request);
        return new ModelAndView("redirect:/cart");
    }

    @PostMapping("/items/{itemId}/remove")
    public ModelAndView removeItem(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                   @PathVariable UUID itemId) {

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        cartService.removeItem(user, itemId);
        return new ModelAndView("redirect:/cart");
    }

    @PostMapping("/place-order")
    public ModelAndView placeOrder(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                   @Valid @ModelAttribute OrderPlacementRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose a delivery address before placing your order.");
            return new ModelAndView("redirect:/cart");
        }

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        orderService.placeOrder(user, request);
        redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully.");
        return new ModelAndView("redirect:/orders");
    }
}
