package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

@Controller
@RequestMapping("/merchant")
public class MerchantController {
    private final UserService userService;

    public MerchantController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{slug}")
    public ModelAndView getMerchantPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                        @PathVariable String slug) {
        ModelAndView modelAndView = new ModelAndView("browse/merchant-page");
        User user = userService.getAuthenticatedUser(authenticationMetadata);

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
