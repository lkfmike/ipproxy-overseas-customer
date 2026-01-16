package com.ipproxy.overseas.customer.cron;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ipproxy.overseas.customer.common.AsnType;
import com.ipproxy.overseas.customer.common.IPQualityType;
import com.ipproxy.overseas.customer.common.StockCache;
import com.ipproxy.overseas.customer.config.SupplierConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockCorn {
    @Resource
    private SupplierConfig supplierConfig;

    @PostConstruct
    public void init() {
        syncStock();
    }

    @Scheduled(cron = "0 */5 * * * ?")
//    @Scheduled(fixedRate = 1000)
    public void updateStock() {
        syncStock();
    }

    public void syncStock() {
        HttpRequest request = HttpRequest.post(supplierConfig.getStaticStockUrl());
        request.header("Authorization", "Bearer " + supplierConfig.getApiToken());
        request.body("{}");
        try (HttpResponse response = request.execute()) {
            List<JSONObject> ispStockList = new ArrayList<>();
            List<JSONObject> hostingStockList = new ArrayList<>();
            String body = response.body();
            JSONArray stockArray = JSONUtil.parseObj(body).getJSONArray("data");
            for (int i = 0; i < stockArray.size(); i++) {
                JSONObject stock = stockArray.getJSONObject(i);
                String quality = stock.getStr("quality");
                String asnType = stock.getStr("asnType");
                if (asnType.equals(AsnType.ISP) && quality.equals(IPQualityType.L4)) {
                    ispStockList.add(stock);
                } else if (asnType.equals(AsnType.HOSTING) && quality.equals(IPQualityType.MAX)) {
                    hostingStockList.add(stock);
                }
            }
            StockCache.STATIC_STOCK_ISP_CACHE = ispStockList;
            StockCache.STATIC_STOCK_HOSTING_CACHE = hostingStockList;
        }
    }
}
