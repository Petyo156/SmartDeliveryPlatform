package org.tuvarna.smartdeliveryplatform.shared.utils;

import java.util.UUID;

public class SlugUtil {
    public static String normalize(String value) {
        return value.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    public static String randomSuffix() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}