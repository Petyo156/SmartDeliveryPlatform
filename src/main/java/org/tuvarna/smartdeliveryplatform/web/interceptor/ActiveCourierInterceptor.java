package org.tuvarna.smartdeliveryplatform.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.InactiveCourierAccessException;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;

@Component
public class ActiveCourierInterceptor implements HandlerInterceptor {

    private final CourierService courierService;

    public ActiveCourierInterceptor(CourierService courierService) {
        this.courierService = courierService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (!(principal instanceof AuthenticationMetadata authMeta) || authMeta.getRole() != UserRole.COURIER) {
            return true;
        }

        if (courierService.courierIsActive(authMeta.getUsername())) {
            return true;
        }

        throw new InactiveCourierAccessException(ExceptionMessages.INACTIVE_COURIER_ACCOUNT);
    }
}
