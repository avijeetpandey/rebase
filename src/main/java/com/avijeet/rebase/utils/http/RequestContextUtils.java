package com.avijeet.rebase.utils.http;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestContextUtils {

    private RequestContextUtils() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

