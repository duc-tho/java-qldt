package com.nhom11.qldt.core.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class HtmxRequestUtils {

    private HtmxRequestUtils() {}

    public static boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
