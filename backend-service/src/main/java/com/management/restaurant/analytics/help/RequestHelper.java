package com.management.restaurant.analytics.help;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestHelper {
    /**
     * Get the client's actual IP address
     * Supporting cases where the app is behind a proxy/load balancer.
     */
    public String getCurrentIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "0.0.0.0";
        }

        // Try popular proxy headers in order of priority
        String[] headerNames = {
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

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // If there are multiple IPs (via multiple proxies), take the first IP.
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback: get the IP directly from the request
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "0.0.0.0";
    }

    /**
     * Get User-Agent from request header
     */
    public String getCurrentUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "Unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && !userAgent.isEmpty() ? userAgent : "Unknown";
    }

    /**
     * Get the current HttpServletRequest from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Check if the IP is valid
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        // Remove unwanted values
        return !ip.equalsIgnoreCase("unknown")
                && !ip.equalsIgnoreCase("null")
                && ip.length() >= 7; // Minimum length of an IP address (e.g., 1.1.1.1)
    }

    /**
     * Lấy User-Agent string
     */
    public String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }

    public String getDeviceInfoString() {
        DeviceInfo info = getDeviceInfo();
        return String.format("%s - %s - %s %s",
                info.getDeviceType(),
                info.getOs(),
                info.getBrowser(),
                info.getDeviceName()
        );
    }

    /**
     * Lấy thông tin device từ User-Agent
     * Parse User-Agent để lấy browser, OS, device type
     */
    public DeviceInfo getDeviceInfo() {
        String userAgent = getCurrentUserAgent();

        if (userAgent == null || userAgent.equals("Unknown")) {
            return new DeviceInfo("Unknown", "Unknown", "Unknown", "Unknown", userAgent);
        }

        String browser = detectBrowser(userAgent);
        String os = detectOS(userAgent);
        String deviceType = detectDeviceType(userAgent);
        String deviceName = detectDeviceName(userAgent);

        return new DeviceInfo(browser, os, deviceType, deviceName, userAgent);
    }

    /**
     * Detect browser from User-Agent
     */
    private String detectBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();

        // Check theo thứ tự cụ thể -> chung (vì nhiều browser dùng chung engine)
        if (ua.contains("edg/") || ua.contains("edge/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("chrome/") && !ua.contains("edg")) {
            return "Chrome";
        }
        if (ua.contains("safari/") && !ua.contains("chrome") && !ua.contains("edg")) {
            return "Safari";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("msie") || ua.contains("trident/")) {
            return "Internet Explorer";
        }
        if (ua.contains("samsung")) {
            return "Samsung Browser";
        }
        if (ua.contains("ucbrowser")) {
            return "UC Browser";
        }

        return "Unknown Browser";
    }

    /**
     * Phát hiện hệ điều hành từ User-Agent
     */
    private String detectOS(String userAgent) {
        String ua = userAgent.toLowerCase();

        // Mobile OS
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return "iOS";
        }

        // Desktop OS
        if (ua.contains("windows nt 10.0")) {
            return "Windows 10/11";
        }
        if (ua.contains("windows nt 6.3")) {
            return "Windows 8.1";
        }
        if (ua.contains("windows nt 6.2")) {
            return "Windows 8";
        }
        if (ua.contains("windows nt 6.1")) {
            return "Windows 7";
        }
        if (ua.contains("windows")) {
            return "Windows";
        }

        if (ua.contains("mac os x")) {
            return "macOS";
        }
        if (ua.contains("mac")) {
            return "Mac";
        }

        if (ua.contains("linux")) {
            return "Linux";
        }
        if (ua.contains("ubuntu")) {
            return "Ubuntu";
        }
        if (ua.contains("fedora")) {
            return "Fedora";
        }

        return "Unknown OS";
    }

    /**
     * Phát hiện loại thiết bị (Mobile, Tablet, Desktop)
     */
    private String detectDeviceType(String userAgent) {
        String ua = userAgent.toLowerCase();

        // Tablet check (phải check trước mobile vì tablet cũng có "mobile" keyword)
        if (ua.contains("ipad") ||
                ua.contains("tablet") ||
                ua.contains("kindle") ||
                (ua.contains("android") && !ua.contains("mobile"))) {
            return "Tablet";
        }

        // Mobile check
        if (ua.contains("mobile") ||
                ua.contains("iphone") ||
                ua.contains("ipod") ||
                ua.contains("android") ||
                ua.contains("webos") ||
                ua.contains("blackberry") ||
                ua.contains("windows phone")) {
            return "Mobile";
        }

        // Desktop
        return "Desktop";
    }

    /**
     * Phát hiện tên thiết bị cụ thể
     */
    private String detectDeviceName(String userAgent) {
        String ua = userAgent.toLowerCase();

        // Apple devices
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("ipad")) return "iPad";
        if (ua.contains("ipod")) return "iPod";
        if (ua.contains("macintosh") || ua.contains("mac os")) return "Mac";

        // Samsung devices
        if (ua.contains("sm-")) {
            // Extract model như SM-G973F, SM-N975F, etc.
            int start = ua.indexOf("sm-");
            int end = ua.indexOf(" ", start);
            if (end == -1) end = ua.indexOf(";", start);
            if (end == -1) end = ua.indexOf(")", start);
            if (end > start) {
                return "Samsung " + ua.substring(start, end).toUpperCase();
            }
            return "Samsung";
        }

        // Other Android devices
        if (ua.contains("pixel")) return "Google Pixel";
        if (ua.contains("nexus")) return "Google Nexus";
        if (ua.contains("huawei")) return "Huawei";
        if (ua.contains("xiaomi")) return "Xiaomi";
        if (ua.contains("oppo")) return "Oppo";
        if (ua.contains("vivo")) return "Vivo";
        if (ua.contains("oneplus")) return "OnePlus";

        // Windows devices
        if (ua.contains("windows")) return "Windows PC";

        // Linux
        if (ua.contains("linux")) return "Linux PC";

        return detectDeviceType(userAgent);
    }

    // Inner class để chứa thông tin device
    public static class DeviceInfo {
        private final String browser;
        private final String os;
        private final String deviceType;
        private final String deviceName;
        private final String userAgent;

        public DeviceInfo(String browser, String os, String deviceType, String deviceName, String userAgent) {
            this.browser = browser;
            this.os = os;
            this.deviceType = deviceType;
            this.deviceName = deviceName;
            this.userAgent = userAgent;
        }

        public String getBrowser() { return browser; }
        public String getOs() { return os; }
        public String getDeviceType() { return deviceType; }
        public String getDeviceName() { return deviceName; }
        public String getUserAgent() { return userAgent; }

        @Override
        public String toString() {
            return String.format("DeviceInfo{browser='%s', os='%s', deviceType='%s', deviceName='%s'}",
                    browser, os, deviceType, deviceName);
        }
    }

    /**
     * Extract device name from User-Agent
     */
    public String extractDeviceName(HttpServletRequest request) {
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