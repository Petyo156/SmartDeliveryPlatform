package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.LoginRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;

@Controller
public class AuthenticationController {
    private final UserService userService;
    private final FlashValidationAttributes flashValidationAttributes;

    public AuthenticationController(UserService userService, FlashValidationAttributes flashValidationAttributes) {
        this.userService = userService;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping("/register")
    public String register(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                           Model model) {
        if (authenticationMetadata != null) {
            return "redirect:/";
        }

        flashValidationAttributes.addModelAttributeIfMissing(model, "registerRequest", new RegisterRequest());
        model.addAttribute("hasHiddenElements", true);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "registerRequest", registerRequest, bindingResult);
            return "redirect:/register";
        }

        userService.register(registerRequest);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String errorParam,
                        @Valid LoginRequest loginRequest,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                        Model model) {

        if (authenticationMetadata != null) {
            return "redirect:/";
        }

        model.addAttribute("error", errorParam);
        model.addAttribute("loginRequest", loginRequest);
        model.addAttribute("hasHiddenElements", true);

        if (errorParam != null || bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Incorrect username or password!");
        }

        return "auth/login";
    }
}
