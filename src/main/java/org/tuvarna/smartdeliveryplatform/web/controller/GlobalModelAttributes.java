package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MerchantService merchantService;

    public GlobalModelAttributes(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @ModelAttribute("merchantIsClosed")
    public boolean merchantClosed() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticationMetadata authMeta) {
            return merchantService.merchantIsClosedStatus(authMeta);
        }

        return false;
    }
}