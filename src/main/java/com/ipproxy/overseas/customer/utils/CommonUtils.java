package com.ipproxy.overseas.customer.utils;

import java.util.UUID;
import java.util.function.Supplier;

public class CommonUtils {
    /**
     * 生成API token
     */
    public static String generateApiToken() {
        Supplier<String> tokenSupplier = () -> "sk-" + UUID.randomUUID().toString().replace("-", "");
        return tokenSupplier.get();
    }
}