package org.tuvarna.smartdeliveryplatform.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class RedirectUrlResolver {

    public String resolveRefererOrDefault(HttpServletRequest request, String defaultUrl) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return defaultUrl;
        }

        try {
            URI uri = URI.create(referer);
            if (!uri.isAbsolute() && !referer.startsWith("/")) {
                return defaultUrl;
            }

            String requestHost = request.getHeader("Host");
            if (uri.isAbsolute() && (requestHost == null || !requestHost.equals(uri.getAuthority()))) {
                return defaultUrl;
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            return query == null ? path : path + "?" + query;
        } catch (IllegalArgumentException e) {
            return defaultUrl;
        }
    }
}
