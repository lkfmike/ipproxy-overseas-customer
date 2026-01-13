package com.ipproxy.overseas.customer.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.ipproxy.overseas.customer.common.AsnType;
import com.ipproxy.overseas.customer.common.StockCache;
import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import com.ipproxy.overseas.customer.entity.system.LocationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemService {
    @Autowired
    private StaticProxyPriceService staticProxyPriceService;

    public LocationResponse.LocationData locations(String type, Long uid) {
        List<JSONObject> stocks = type.equals(AsnType.ISP) ? StockCache.STATIC_STOCK_ISP_CACHE : StockCache.STATIC_STOCK_HOSTING_CACHE;
        List<StaticProxyPrice> prices = staticProxyPriceService.getProxyPriceListByUid(uid);
        Map<String, JSONArray> groupedStocks = new HashMap<>();
        for (int i = 0; i < stocks.size(); i++) {
            JSONObject stock = stocks.get(i);
            String state = stock.getStr("state", "其他");
            String area = stock.getStr("area");
            String region = stock.getStr("region");
            String asnType = stock.getStr("asnType");
            String quality = stock.getStr("quality");
            BigDecimal price = prices.stream().filter(p -> p.getType().equals(asnType) && p.getQuality().equals(quality) && p.getArea().equals(area) && p.getRegion().equals(region)).findFirst().map(StaticProxyPrice::getPrice).orElse(new BigDecimal(9999));
            BigDecimal discountPrice = prices.stream().filter(p -> p.getType().equals(asnType) && p.getQuality().equals(quality) && p.getArea().equals(area) && p.getRegion().equals(region)).findFirst().map(StaticProxyPrice::getDiscountPrice).orElse(new BigDecimal(9999));
            stock.set("price", price);
            stock.set("discountPrice", discountPrice);
            groupedStocks.computeIfAbsent(state, k -> new JSONArray()).put(stock);
        }
        return null;
    }
}