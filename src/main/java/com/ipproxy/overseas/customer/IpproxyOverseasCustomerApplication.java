package com.ipproxy.overseas.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IpproxyOverseasCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IpproxyOverseasCustomerApplication.class, args);
    }
}

