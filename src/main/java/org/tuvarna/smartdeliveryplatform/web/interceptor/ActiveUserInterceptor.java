package org.tuvarna.smartdeliveryplatform.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;

import java.io.IOException;

@Component
public class ActiveUserInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public ActiveUserInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();

        if (!(principal instanceof AuthenticationMetadata authMeta)) {
            return true;
        }

        User user = userService.getUserByEmail(authMeta.getUsername());
        if (user.getStatus() == UserStatus.ACTIVE) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
