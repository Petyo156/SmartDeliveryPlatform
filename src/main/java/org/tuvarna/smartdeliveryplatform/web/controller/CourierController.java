package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

@Controller
@RequestMapping("/courier")
public class CourierController {
    private final UserService userService;
    private final CourierService courierService;

    public CourierController(UserService userService, CourierService courierService) {
        this.userService = userService;
        this.courierService = courierService;
    }

    @GetMapping("/orders")
    public ModelAndView getMyShopPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("courier/courier-orders");
        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
