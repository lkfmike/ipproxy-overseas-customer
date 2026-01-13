package com.ipproxy.overseas.customer.entity.system;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class LocationResponse {
    private String state;
    private List<Country> countries;

    @Data
    @Builder
    public static class Country {
        private int id;
        private String area;
        private String areaCn;
        private String region;
        private String regionCn;
        private boolean available;
        private double price;
        private double discountPrice;
    }
}