package com.ipproxy.overseas.customer.common;

public class ProxyProtocolType {
    public static final int SOCKS5 = 1;
    public static final int HTTP = 2;
    public static final int VMESS = 3;
    public static final int VLESS = 4;
    public static final String SOCKS5_NAME = "SOCKS";
    public static final String HTTP_NAME = "HTTP";
    public static final String VMESS_NAME = "VMESS";
    public static final String VLESS_NAME = "VLESS";

    public static boolean isValid(int type) {
        return type == SOCKS5 || type == HTTP || type == VMESS || type == VLESS;
    }

    public static String getName(int type) {
        switch (type) {
            case SOCKS5:
                return SOCKS5_NAME;
            case HTTP:
                return HTTP_NAME;
            case VMESS:
                return VMESS_NAME;
            case VLESS:
                return VLESS_NAME;
            default:
                return "";
        }
    }

    public static int getSocksValue() {
        return SOCKS5;
    }

    public static int getValueByName(String name) {
        if (SOCKS5_NAME.equals(name)) {
            return SOCKS5;
        } else if (HTTP_NAME.equals(name)) {
            return HTTP;
        } else if (VMESS_NAME.equals(name)) {
            return VMESS;
        } else if (VLESS_NAME.equals(name)) {
            return VLESS;
        }
        return SOCKS5; // Return -1 if name is not found
    }
}
