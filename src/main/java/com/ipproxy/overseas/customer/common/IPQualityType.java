package com.ipproxy.overseas.customer.common;

/**
 * IP质量度
 */
public class IPQualityType {
    /* 普通 */
    public static final String COMMON = "L1";
    /* 优质 */
    public static final String PRO = "L2";
    /* 极优 */
    public static final String MAX = "L3";
    /* 原生*/
    public static final String L4 = "L4";
    /* PPPOE */
    public static final String L5 = "L5";

    public static String getQualityName(String quality) {
        switch (quality) {
            case COMMON:
                return "普通";
            case PRO:
                return "优质";
            case MAX:
                return "极优";
            case L4:
                return "原生";
            case L5:
                return "PPPOE";
            default:
                return "-";
        }
    }
}
