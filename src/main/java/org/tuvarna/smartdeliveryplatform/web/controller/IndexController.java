package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {
    private final UserService userService;
    private final MerchantService merchantService;

    public IndexController(UserService userService, MerchantService merchantService) {
        this.userService = userService;
        this.merchantService = merchantService;
    }

    @GetMapping()
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("home/index");
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/shops")
    public ModelAndView getShopsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @RequestParam(required = false) String category) {
        ModelAndView modelAndView = new ModelAndView("home/shops");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveShops();

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantCardResponses", merchantCardResponses);
        return modelAndView;
    }

    @GetMapping("/restaurants")
    public ModelAndView getRestaurantsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                           @RequestParam(required = false) String category) {
        ModelAndView modelAndView = new ModelAndView("home/restaurants");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveRestaurants();

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantCardResponses", merchantCardResponses);
        return modelAndView;
    }
}