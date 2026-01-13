package com.ipproxy.overseas.customer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "supplier")
@Component
@Getter
@Setter
public class SupplierConfig {
    private String apiToken;
    private String staticStockUrl;
}
