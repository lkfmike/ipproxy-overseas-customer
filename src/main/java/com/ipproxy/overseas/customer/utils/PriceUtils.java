package com.ipproxy.overseas.customer.utils;

import com.ipproxy.overseas.customer.entity.StaticProxyPrice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSONObject;

public class PriceUtils {
    
    /**
     * 构建价格键，用于快速查找
     */
    public static String buildPriceKey(String type, String quality, String area, String region) {
        return String.format("%s_%s_%s_%s", type, quality, area, region);
    }
    
    /**
     * 构建价格映射，用于快速查找
     */
    public static Map<String, StaticProxyPrice> buildPriceMap(List<StaticProxyPrice> prices) {
        Map<String, StaticProxyPrice> priceMap = new HashMap<>();
        for (StaticProxyPrice price : prices) {
            String key = buildPriceKey(price.getType(), price.getQuality(), price.getArea(), price.getRegion());
            priceMap.put(key, price);
        }
        return priceMap;
    }
    
    /**
     * 从stock对象中提取信息并构建价格键
     */
    public static String buildPriceKeyFromStock(JSONObject stock) {
        String area = stock.getStr("area");
        String region = stock.getStr("region");
        String asnType = stock.getStr("asnType");
        String quality = stock.getStr("quality");
        return buildPriceKey(asnType, quality, area, region);
    }
}