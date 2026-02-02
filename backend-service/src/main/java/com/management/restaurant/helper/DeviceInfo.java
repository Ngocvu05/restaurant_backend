package com.management.restaurant.helper;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * Extract client IP address
 */
public class DeviceInfo {
    private String extractClientIp(HttpServletRequest request) {
        String[] IP_HEADER_CANDIDATES = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Extract User Agent
     */
    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : null;
    }

    /**
     * Extract or generate Device ID
     */
    private String extractDeviceId(HttpServletRequest request) {
        // Try to get from custom header first
        String deviceId = request.getHeader("X-Device-Id");

        // If not provided, generate based on User-Agent and IP
        if (deviceId == null || deviceId.isEmpty()) {
            String userAgent = extractUserAgent(request);
            String ip = extractClientIp(request);
            deviceId = UUID.nameUUIDFromBytes((userAgent + ip).getBytes()).toString();
        }

        return deviceId;
    }

    /**
     * Extract device name from User-Agent
     */
    private String extractDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown Device";
        }

        // Parse User-Agent to extract device info
        if (userAgent.contains("Mobile")) {
            if (userAgent.contains("iPhone")) return "iPhone";
            if (userAgent.contains("iPad")) return "iPad";
            if (userAgent.contains("Android")) return "Android Mobile";
            return "Mobile Device";
        }

        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Linux")) return "Linux PC";
        if (userAgent.contains("Chrome")) return "Chrome Browser";
        if (userAgent.contains("Firefox")) return "Firefox Browser";
        if (userAgent.contains("Safari")) return "Safari Browser";

        return "Unknown Device";
    }
}