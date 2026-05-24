package com.artdesign.common.core.page;

import java.util.Map;

public final class PageUtils {
    private static final long DEFAULT_PAGE_NUM = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 500L;

    private PageUtils() {
    }

    public static long pageNum(Map<String, String> params) {
        return parsePositive(params == null ? null : params.get("pageNum"), DEFAULT_PAGE_NUM);
    }

    public static long pageSize(Map<String, String> params) {
        long pageSize = parsePositive(params == null ? null : params.get("pageSize"), DEFAULT_PAGE_SIZE);
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private static long parsePositive(String value, long fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
