package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

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
        Boolean merchantIsClosed = merchantService.merchantIsClosedStatus(authenticationMetadata);

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantIsClosed", merchantIsClosed);
        return modelAndView;
    }
}