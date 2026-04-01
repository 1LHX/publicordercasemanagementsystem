package com.example.publicordercasemanagementsystem.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private RequestUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }

    public static UserAgentInfo parseUserAgent(String userAgent) {
        if (userAgent == null) {
            return new UserAgentInfo("Unknown", "Unknown", "Unknown");
        }
        String lower = userAgent.toLowerCase();
        String os = "Unknown";
        if (lower.contains("windows")) {
            os = "Windows";
        } else if (lower.contains("mac os x") || lower.contains("macintosh")) {
            os = "macOS";
        } else if (lower.contains("android")) {
            os = "Android";
        } else if (lower.contains("iphone") || lower.contains("ipad")) {
            os = "iOS";
        }

        String browser = "Unknown";
        if (lower.contains("edg")) {
            browser = "Edge";
        } else if (lower.contains("chrome")) {
            browser = "Chrome";
        } else if (lower.contains("firefox")) {
            browser = "Firefox";
        } else if (lower.contains("msie") || lower.contains("trident")) {
            browser = "IE";
        } else if (lower.contains("safari")) {
            browser = "Safari";
        }

        String deviceType = (lower.contains("mobile") || os.equals("Android") || os.equals("iOS"))
                ? "Mobile" : "Desktop";

        return new UserAgentInfo(deviceType, browser, os);
    }

    public record UserAgentInfo(String deviceType, String browser, String os) {
    }
}
