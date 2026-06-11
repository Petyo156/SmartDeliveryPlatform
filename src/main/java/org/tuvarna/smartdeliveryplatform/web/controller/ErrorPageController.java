package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

@Controller
public class ErrorPageController {

    private final UserService userService;

    public ErrorPageController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/error/404")
    public String notFound(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, Model model) {
        model.addAttribute("user", userService.getAuthenticatedUser(authenticationMetadata));
        return "exception/not-found";
    }
}
