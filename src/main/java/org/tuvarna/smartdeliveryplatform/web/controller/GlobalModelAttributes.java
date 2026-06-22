package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MerchantService merchantService;
    private final CourierService courierService;
    private final UserService userService;

    public GlobalModelAttributes(MerchantService merchantService,
                                 CourierService courierService,
                                 UserService userService) {
        this.merchantService = merchantService;
        this.courierService = courierService;
        this.userService = userService;
    }

    @ModelAttribute("user")
    public User user() {
        AuthenticationMetadata authMeta = getAuthenticationMetadata();
        if (authMeta == null) {
            return null;
        }

        return userService.getAuthenticatedUser(authMeta);
    }

    @ModelAttribute("merchantIsClosed")
    public boolean merchantClosed() {
        AuthenticationMetadata authMeta = getAuthenticationMetadata();
        if (authMeta == null || authMeta.getRole() != UserRole.MERCHANT) {
            return false;
        }

        return merchantService.merchantIsClosedStatus(authMeta);
    }

    @ModelAttribute("merchantIsActive")
    public boolean merchantActive() {
        AuthenticationMetadata authMeta = getAuthenticationMetadata();
        if (authMeta == null || authMeta.getRole() != UserRole.MERCHANT) {
            return false;
        }

        return merchantService.merchantIsActive(authMeta.getUsername());
    }

    @ModelAttribute("courierIsAvailable")
    public boolean courierAvailable() {
        AuthenticationMetadata authMeta = getAuthenticationMetadata();
        if (authMeta == null || authMeta.getRole() != UserRole.COURIER) {
            return false;
        }

        return courierService.courierIsAvailable(authMeta);
    }

    @ModelAttribute("courierIsActive")
    public boolean courierActive() {
        AuthenticationMetadata authMeta = getAuthenticationMetadata();
        if (authMeta == null || authMeta.getRole() != UserRole.COURIER) {
            return false;
        }

        return courierService.courierIsActive(authMeta.getUsername());
    }

    private AuthenticationMetadata getAuthenticationMetadata() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticationMetadata authMeta) {
            return authMeta;
        }

        return null;
    }
}
