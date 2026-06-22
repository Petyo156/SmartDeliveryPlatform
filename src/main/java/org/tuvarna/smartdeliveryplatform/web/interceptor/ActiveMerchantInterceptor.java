package org.tuvarna.smartdeliveryplatform.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.InactiveMerchantAccessException;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;

@Component
public class ActiveMerchantInterceptor implements HandlerInterceptor {

    private final MerchantService merchantService;

    public ActiveMerchantInterceptor(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (!(principal instanceof AuthenticationMetadata authMeta) || authMeta.getRole() != UserRole.MERCHANT) {
            return true;
        }

        if (merchantService.merchantIsActive(authMeta.getUsername())) {
            return true;
        }

        throw new InactiveMerchantAccessException(ExceptionMessages.INACTIVE_MERCHANT_ACCOUNT);
    }
}
