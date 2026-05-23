package com.artdesign.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public final class ServletUtils {
    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "X-Real-IP"
    );

    private ServletUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                int commaIndex = value.indexOf(',');
                return commaIndex >= 0 ? value.substring(0, commaIndex).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
