package com.ipproxy.overseas.customer.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSource.class)
@MapperScan("com.ipproxy.overseas.customer.mapper")
public class MybatisConfig {
}

