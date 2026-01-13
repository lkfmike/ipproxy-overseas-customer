package com.ipproxy.overseas.customer.entity.system;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class LocationResponse {
    
    @Data
    @Builder
    public static class LocationData {
        private List<Region> regions;
    }
    
    @Data
    @Builder
    public static class Region {
        private String name;
        private List<Country> countries;
    }
    
    @Data
    @Builder
    public static class Country {
        private String name;
        private String code;
        private List<City> cities;
    }
    
    @Data
    @Builder
    public static class City {
        private String name;
        private String code;
        private boolean available;
        private Double price;
    }
}