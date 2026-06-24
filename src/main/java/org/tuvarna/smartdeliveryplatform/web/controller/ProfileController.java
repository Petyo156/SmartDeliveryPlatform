package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.constants.SuccessMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.ProfileService;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserProfileRequest;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;
    private final FlashValidationAttributes flashValidationAttributes;

    public ProfileController(ProfileService profileService,
                             UserService userService,
                             FlashValidationAttributes flashValidationAttributes) {
        this.profileService = profileService;
        this.userService = userService;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping
    public String getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                 Model model) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        UserProfileRequest profileRequest = profileService.initializeProfileRequest(user);
        flashValidationAttributes.addModelAttributeIfMissing(model, "profileRequest", profileRequest);

        return "profile/profile";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @Valid @ModelAttribute("profileRequest") UserProfileRequest profileRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "profileRequest", profileRequest, bindingResult);
            return "redirect:/profile";
        }

        profileService.updateUserProfile(authenticationMetadata.getUsername(), profileRequest);
        redirectAttributes.addFlashAttribute("successMessage", SuccessMessages.PROFILE_UPDATED);
        return "redirect:/profile";
    }
}
