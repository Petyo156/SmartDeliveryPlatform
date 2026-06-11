package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.ProfileService;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserProfileRequest;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("profile/profile");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        UserProfileRequest profileRequest = profileService.initializeProfileRequest(user);
        modelAndView.addObject("profileRequest", profileRequest);

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView updateProfile(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @Valid @ModelAttribute("profileRequest") UserProfileRequest profileRequest,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile/profile");
            User user = userService.getAuthenticatedUser(authenticationMetadata);
            modelAndView.addObject("profileRequest", profileRequest);
            return modelAndView;
        }

        profileService.updateUserProfile(authenticationMetadata.getUsername(), profileRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return new ModelAndView("redirect:/profile");
    }
}
