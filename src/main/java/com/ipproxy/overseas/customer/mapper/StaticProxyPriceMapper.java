package com.ipproxy.overseas.customer.mapper;

import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StaticProxyPriceMapper {
    StaticProxyPrice selectByUidAndType(@Param("uid") Long uid, @Param("type") String type);

    BigDecimal getDefaultPrice(@Param("type") String type);

    StaticProxyPrice selectDefaultByType(@Param("type") String type);

    List<StaticProxyPrice> selectByUid(@Param("uid") Long uid);

    List<StaticProxyPrice> selectAllDefaultPrices();
}