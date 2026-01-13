package com.ipproxy.overseas.customer.entity;

import lombok.Data;

@Data
public class AreaRegionInfo {
    private Integer id;
    private String area;        // 区域代码，如 US
    private String areaCn;      // 区域中文名，如 美国
    private String region;      // 地区代码，如 New York
    private String regionCn;    // 地区中文名，如 纽约
    private Integer isShow;     // 是否显示
    private Integer isAvailable; // 是否可用
}