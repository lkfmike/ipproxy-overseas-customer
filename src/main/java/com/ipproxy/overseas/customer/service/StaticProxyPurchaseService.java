package com.ipproxy.overseas.customer.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ipproxy.overseas.customer.common.AsnType;
import com.ipproxy.overseas.customer.common.Constants;
import com.ipproxy.overseas.customer.common.ProxyProtocolType;
import com.ipproxy.overseas.customer.common.StockCache;
import com.ipproxy.overseas.customer.config.SupplierConfig;
import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.entity.AsyncOrder;
import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import com.ipproxy.overseas.customer.entity.proxy.PurchaseStaticProxyRequest;
import com.ipproxy.overseas.customer.entity.proxy.PurchaseStaticProxyResponse;
import com.ipproxy.overseas.customer.exception.PurchaseException;
import com.ipproxy.overseas.customer.mapper.AsyncOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class StaticProxyPurchaseService {
    @Autowired
    private StaticProxyPriceService staticProxyPriceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SupplierConfig supplierConfig;
    @Autowired
    private AsyncOrderMapper asyncOrderMapper;

    public PurchaseStaticProxyResponse purchaseStaticProxy(Long userId, PurchaseStaticProxyRequest request) throws PurchaseException {
        // 获取库存信息
        JSONObject stock = getStockById(request.getStockId());
        if (stock == null) {
            throw new PurchaseException("库存不存在");
        }
        // 获取用户账户信息，检查余额
        Account account = accountService.getAccount(userId);
        if (account == null) {
            throw new PurchaseException("用户不存在");
        }
        // 获取价格信息
        Map<String, StaticProxyPrice> priceMap = buildPriceMap(userId);
        // 计算基础价格
        String area = stock.getStr("area");
        String region = stock.getStr("region");
        String asnType = stock.getStr("asnType");
        String quality = stock.getStr("quality");
        String priceKey = buildPriceKey(asnType, quality, area, region);
        StaticProxyPrice priceInfo = priceMap.get(priceKey);
        BigDecimal basePrice = new BigDecimal("0");
        if (priceInfo != null && priceInfo.getPrice() != null) {
            basePrice = priceInfo.getPrice();
        } else {
            basePrice = new BigDecimal("9999"); // 默认价格
        }
        // 计算天数系数和折扣
        DaysFactorResult daysFactorResult = calculateDaysFactor(request.getPeriod());
        BigDecimal daysFactor = daysFactorResult.getFactor();
        BigDecimal discountRate = daysFactorResult.getDiscount();
        // 计算总的基础价格（单价 * 数量 * 天数系数）
        BigDecimal totalBasePrice = basePrice.multiply(new BigDecimal(request.getQuantity())).multiply(daysFactor);
        // 计算专线带宽费用
        BigDecimal rawBandwidthPrice = BigDecimal.ZERO;
        if (request.getDedicatedLine() != null && request.getDedicatedLine() && request.getBandwidth() != null && !request.getBandwidth().trim().isEmpty()) {
            rawBandwidthPrice = calculateBandwidthPrice(request.getBandwidth(), request.getQuantity(), request.getPeriod());
        }
        // 应用折扣到基础价格和带宽费用
        BigDecimal discountedBasePrice = totalBasePrice.multiply(discountRate);
        BigDecimal discountedBandwidthPrice = rawBandwidthPrice.multiply(discountRate);
        // 计算折扣（基于折扣差额）
        BigDecimal discount = totalBasePrice.add(rawBandwidthPrice).subtract(discountedBasePrice.add(discountedBandwidthPrice));
        // 计算总金额
        BigDecimal totalAmount = calculateFinalAmount(discountedBasePrice, discountedBandwidthPrice, BigDecimal.ZERO);
        // 检查余额是否充足
        BigDecimal formattedBalance = account.getBalance().setScale(2, RoundingMode.HALF_UP);
        BigDecimal formattedTotalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        if (account.getBalance().compareTo(totalAmount) < 0) {
            throw new PurchaseException("余额不足，当前余额: " + formattedBalance + ", 所需金额: " + formattedTotalAmount);
        }
        //余额充足,向IP供应商发送采购请求
        HttpRequest post = HttpRequest.post(supplierConfig.getStaticBatchPurchaseUrl());
        post.header("Content-Type", "application/json");
        post.header("Authorization", "Bearer " + supplierConfig.getApiToken());
        post.body(new JSONObject()
                .set("areaId", request.getStockId())
                .set("batchSize", request.getQuantity())
                .set("protocol", ProxyProtocolType.getValueByName(request.getProtocol()))
                .set("dedicatedLine", request.getDedicatedLine())
                .set("bandwidth", request.getBandwidth())
                .set("autoRenewal", request.getAutoRenew())
                .set("months", request.getPeriod() / 30)
                .toString());
        String orderNo;
        try (HttpResponse httpResponse = post.execute()) {
            JSONObject remoteResponse = JSONUtil.parseObj(httpResponse.body());
            if (remoteResponse.getInt("code") != 200) {
                log.warn("static proxy batch  purchase failed: " + remoteResponse);
                throw new PurchaseException("购买失败,请联系管理员");
            }
            JSONObject data = remoteResponse.getJSONObject("data");
            Boolean success = data.getBool("isSuccess");
            if (!success) {
                log.warn("static proxy batch  purchase failed: " + remoteResponse);
                throw new PurchaseException("购买失败,请联系管理员");
            }
            orderNo = data.getStr("orderNo");
        } catch (Exception e) {
            log.warn("static proxy batch  purchase failed: " + e.getMessage());
            throw new PurchaseException("购买失败,请联系管理员");
        }
        System.out.println(orderNo);
        //调用供应商成功之后,更新余额,创建订单
        // 保存订单到数据库
        AsyncOrder order = new AsyncOrder();
        order.setOrderNo(orderNo);  // 供应商返回的订单号
        order.setSystemOrderNo("order_" + System.currentTimeMillis() + "_" + userId);  // 系统生成的订单号
        order.setUid(userId);
        BigDecimal unitPrice = basePrice.multiply(daysFactor).multiply(discountRate);  // 计算实际单价
        order.setUnitPrice(unitPrice);
        order.setQuantity(request.getQuantity());
        order.setAutoRenew(request.getAutoRenew());
        order.setProtocol(request.getProtocol()); // 设置协议
        order.setAsnType(stock.getStr("asnType"));
        order.setQuality(stock.getStr("quality"));
        order.setStockId(request.getStockId());
        order.setStatus("completed");
        asyncOrderMapper.insert(order);
        // 构建响应
        PurchaseStaticProxyResponse.Detail detail = new PurchaseStaticProxyResponse.Detail();
        detail.setBasePrice(discountedBasePrice.setScale(2, RoundingMode.HALF_UP));
        detail.setBandwidthPrice(discountedBandwidthPrice.setScale(2, RoundingMode.HALF_UP));
        detail.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        PurchaseStaticProxyResponse response = new PurchaseStaticProxyResponse();
        response.setOrderId("order_" + System.currentTimeMillis() + "_" + userId);
        response.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        response.setDetail(detail);
        return response;
    }


    private BigDecimal calculateFinalAmount(BigDecimal basePrice, BigDecimal bandwidthPrice, BigDecimal discount) {
        BigDecimal totalAmount = basePrice.add(bandwidthPrice).subtract(discount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, StaticProxyPrice> buildPriceMap(Long userId) {
        List<StaticProxyPrice> prices = staticProxyPriceService.getProxyPriceListByUid(userId);
        Map<String, StaticProxyPrice> priceMap = new HashMap<>();
        for (StaticProxyPrice price : prices) {
            String key = buildPriceKey(price.getType(), price.getQuality(), price.getArea(), price.getRegion());
            priceMap.put(key, price);
        }
        return priceMap;
    }

    private JSONObject getStockById(Integer stockId) {
        // 遍历ISP和HOSTING库存以查找指定ID的库存
        List<JSONObject> allStocks = new ArrayList<>();
        allStocks.addAll(StockCache.STATIC_STOCK_ISP_CACHE);
        allStocks.addAll(StockCache.STATIC_STOCK_HOSTING_CACHE);

        for (JSONObject stock : allStocks) {
            if (stock.getInt("id").equals(stockId)) {
                return stock;
            }
        }
        return null;
    }

    private String buildPriceKey(String type, String quality, String area, String region) {
        return String.format("%s_%s_%s_%s", type, quality, area, region);
    }

    /**
     * 根据使用天数计算天数系数
     * 30天=1倍(无折扣), 60天=2倍(95折), 120天=4倍(93折), 360天=12倍(9折)
     */
    private DaysFactorResult calculateDaysFactor(Integer period) {
        if (period == null) {
            throw new IllegalArgumentException("使用期限不能为空");
        }

        switch (period) {
            case 30:
                return new DaysFactorResult(new BigDecimal("1.0"), new BigDecimal("1.0")); // 无折扣
            case 60:
                return new DaysFactorResult(new BigDecimal("2.0"), new BigDecimal("0.95")); // 95折
            case 120:
                return new DaysFactorResult(new BigDecimal("4.0"), new BigDecimal("0.93")); // 93折
            case 360:
                return new DaysFactorResult(new BigDecimal("12.0"), new BigDecimal("0.90")); // 9折
            default:
                throw new IllegalArgumentException("不支持的使用期限: " + period + ", 只支持30, 60, 120, 360天");
        }
    }

    /**
     * 计算专线带宽费用
     */
    private BigDecimal calculateBandwidthPrice(String bandwidth, Integer quantity, Integer period) {
        if (bandwidth == null) {
            throw new IllegalArgumentException("带宽规格不能为空");
        }

        BigDecimal pricePerUnit = BigDecimal.ZERO;
        switch (bandwidth) {
            case "3M":
                pricePerUnit = new BigDecimal("1.5");
                break;
            case "5M":
                pricePerUnit = new BigDecimal("3");
                break;
            case "10M":
                pricePerUnit = new BigDecimal("15");
                break;
            default:
                throw new IllegalArgumentException("不支持的带宽规格: " + bandwidth + ", 只支持3M, 5M, 10M");
        }

        // 基于数量和使用期限计算总价
        DaysFactorResult daysFactorResult = calculateDaysFactor(period);
        BigDecimal daysFactor = daysFactorResult.getFactor();
        return pricePerUnit.multiply(new BigDecimal(quantity)).multiply(daysFactor).setScale(2, RoundingMode.HALF_UP);
    }
}