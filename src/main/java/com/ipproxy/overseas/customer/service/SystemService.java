package com.ipproxy.overseas.customer.service;

import cn.hutool.json.JSONObject;
import com.ipproxy.overseas.customer.common.AsnType;
import com.ipproxy.overseas.customer.common.StockCache;
import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import com.ipproxy.overseas.customer.entity.system.LocationResponse;
import com.ipproxy.overseas.customer.utils.PriceUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemService {
    @Resource
    private StaticProxyPriceService staticProxyPriceService;

    public List<LocationResponse> locations(String type, Long uid) {
        List<JSONObject> stocks = type.equals(AsnType.ISP) ? StockCache.STATIC_STOCK_ISP_CACHE : StockCache.STATIC_STOCK_HOSTING_CACHE;
        List<StaticProxyPrice> prices = staticProxyPriceService.getProxyPriceListByUid(uid);
        // 创建价格映射以提高查找效率
        Map<String, StaticProxyPrice> priceMap = PriceUtils.buildPriceMap(prices);
        // 按状态分组股票
        Map<String, List<JSONObject>> groupedStocks = new HashMap<>();
        for (JSONObject stock : stocks) {
            String state = stock.getStr("state", "其他");
            groupedStocks.computeIfAbsent(state, k -> new ArrayList<>()).add(stock);
        }
        List<LocationResponse> list = new ArrayList<>();
        for (Map.Entry<String, List<JSONObject>> entry : groupedStocks.entrySet()) {
            String state = entry.getKey();
            List<JSONObject> stockList = entry.getValue();
            List<LocationResponse.Country> countries = new ArrayList<>();
            for (JSONObject stock : stockList) {
                String priceKey = PriceUtils.buildPriceKeyFromStock(stock);
                StaticProxyPrice priceInfo = priceMap.get(priceKey);
                BigDecimal price = new BigDecimal(9999);
                BigDecimal discountPrice = new BigDecimal(9999);
                if (priceInfo != null) {
                    price = priceInfo.getPrice() != null ? priceInfo.getPrice() : new BigDecimal(9999);
                    discountPrice = priceInfo.getDiscountPrice() != null ? priceInfo.getDiscountPrice() : new BigDecimal(9999);
                }
                stock.set("price", price);
                stock.set("discountPrice", discountPrice);
                LocationResponse.Country country = LocationResponse.Country.builder()
                        .id(stock.getInt("id"))
                        .area(stock.getStr("area"))
                        .areaCn(stock.getStr("areaCn"))
                        .region(stock.getStr("region"))
                        .regionCn(stock.getStr("regionCn"))
                        .available(stock.getInt("status") == 1)
                        .price(price.doubleValue())
                        .discountPrice(discountPrice.doubleValue())
                        .build();
                countries.add(country);
            }
            LocationResponse response = LocationResponse.builder()
                    .state(state)
                    .countries(countries)
                    .build();
            list.add(response);
        }
        return list;
    }
}