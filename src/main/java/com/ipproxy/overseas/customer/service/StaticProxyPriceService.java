package com.ipproxy.overseas.customer.service;

import com.ipproxy.overseas.customer.entity.AreaRegionInfo;
import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import com.ipproxy.overseas.customer.mapper.AreaRegionInfoMapper;
import com.ipproxy.overseas.customer.mapper.StaticProxyPriceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Service
public class StaticProxyPriceService {
    @Resource
    private StaticProxyPriceMapper staticProxyPriceMapper;
    @Resource
    private AreaRegionInfoMapper areaRegionInfoMapper;

    /**
     * 获取用户指定类型的代理价格
     * 如果用户设置了特定价格，则返回用户价格；否则返回默认价格
     *
     * @param uid  用户ID
     * @param type 代理类型
     * @return 代理价格
     */
    public BigDecimal getProxyPrice(Long uid, String type) {
        // 首先尝试获取用户特定价格
        StaticProxyPrice userPrice = staticProxyPriceMapper.selectByUidAndType(uid, type);
        if (userPrice != null && userPrice.getPrice() != null) {
            // 如果用户设置了特定价格，返回用户价格
            return userPrice.getPrice();
        } else {
            // 否则返回默认价格
            BigDecimal defaultPrice = staticProxyPriceMapper.getDefaultPrice(type);
            if (defaultPrice != null) {
                return defaultPrice;
            } else {
                // 如果没有默认价格，返回一个默认值（如0）
                return new BigDecimal("0.00");
            }
        }
    }

    /**
     * 获取用户的所有代理价格列表
     *
     * @param uid 用户ID
     * @return 价格列表
     */
    public List<StaticProxyPrice> getProxyPriceListByUid(Long uid) {
        // 获取当前用户的价格配置和默认价格配置
        List<StaticProxyPrice> currentUserPrices = staticProxyPriceMapper.selectByUid(uid);
        List<StaticProxyPrice> defaultPrices = staticProxyPriceMapper.selectAllDefaultPrices();
        List<StaticProxyPrice> allCustomerPrices = new ArrayList<>();
        allCustomerPrices.addAll(currentUserPrices);
        allCustomerPrices.addAll(defaultPrices);
        // 使用Map去重，优先级：用户特定价格 > 默认价格
        Map<String, StaticProxyPrice> priceMap = new HashMap<>();
        for (StaticProxyPrice priceInfo : allCustomerPrices) {
            String key = getPriceAggrKey(priceInfo.getUid(), priceInfo.getType(), priceInfo.getQuality(), priceInfo.getArea(), priceInfo.getRegion());
            if (!priceMap.containsKey(key)) {
                priceMap.put(key, priceInfo);
            }
        }
        // 获取区域信息，用于初始化所有可能的价格配置
        List<AreaRegionInfo> areaRegionList = areaRegionInfoMapper.selectAll();
        // 根据IP类型+质量度+国家+省州, 初始化单价信息
        List<StaticProxyPrice> priceInfoList = new ArrayList<>();
        // IP类型
        for (String asnType : Arrays.asList("isp", "hosting")) {  // 简化的类型列表
            // IP质量度
            for (String quality : Arrays.asList("L3", "L4")) {  // 简化的质量度列表
                // 遍历区域信息
                for (AreaRegionInfo areaRegionInfo : areaRegionList) {
                    // 从配置的价格表中查询价格配置信息
                    StaticProxyPrice priceRecord = searchPrice(asnType, quality, uid, areaRegionInfo.getArea(), areaRegionInfo.getRegion(), priceMap);
                    StaticProxyPrice priceInfo = new StaticProxyPrice();
                    priceInfo.setUid(uid);
                    priceInfo.setType(asnType);
                    priceInfo.setQuality(quality);
                    priceInfo.setArea(areaRegionInfo.getArea());
                    priceInfo.setRegion(areaRegionInfo.getRegion());
                    if (priceRecord != null) {
                        priceInfo.setPrice(priceRecord.getPrice());
                        priceInfo.setDiscountPrice(priceRecord.getDiscountPrice());
                    } else {
                        // 如果没有特定配置，设置默认值或从其他地方获取
                        priceInfo.setPrice(new BigDecimal("9999.00")); // 默认不可购买价格
                        priceInfo.setDiscountPrice(new BigDecimal("9999.00"));
                    }
                    priceInfoList.add(priceInfo);
                }
            }
        }

        return priceInfoList;
    }

    /**
     * 根据聚合键搜索价格配置
     */
    public StaticProxyPrice searchPrice(String type, String quality, Long uid, String area, String region, Map<String, StaticProxyPrice> priceMap) {
        // 构建检索顺序
        List<Long> uidSearchOrder = Arrays.asList(uid, 0L);
        List<String> qualitySearchOrder = Arrays.asList(quality, "default");
        List<String> areaSearchOrder = Arrays.asList(area, "default");
        List<String> regionSearchOrder = Arrays.asList(region, "default");
        // 顺序检索
        for (Long uidKey : uidSearchOrder) {
            for (String qualityKey : qualitySearchOrder) {
                for (String areaKey : areaSearchOrder) {
                    for (String regionKey : regionSearchOrder) {
                        String key = getPriceAggrKey(uidKey, type, qualityKey, areaKey, regionKey);
                        if (priceMap.containsKey(key)) {
                            return priceMap.get(key);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 生成价格聚合键
     */
    private String getPriceAggrKey(Long uid, String type, String quality, String area, String region) {
        return String.format("%s_%s_%s_%s_%s", uid, type, quality, area, region);
    }
}