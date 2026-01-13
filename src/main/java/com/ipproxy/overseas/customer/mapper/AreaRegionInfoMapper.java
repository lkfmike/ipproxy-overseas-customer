package com.ipproxy.overseas.customer.mapper;

import com.ipproxy.overseas.customer.entity.AreaRegionInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AreaRegionInfoMapper {
    List<AreaRegionInfo> selectAll();

    List<AreaRegionInfo> selectByArea(@Param("area") String area);

    List<AreaRegionInfo> selectByAreaRegion(@Param("area") String area, @Param("region") String region);
}