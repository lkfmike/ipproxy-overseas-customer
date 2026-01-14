package com.ipproxy.overseas.customer.mapper;

import com.ipproxy.overseas.customer.entity.AsyncOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AsyncOrderMapper {

    void insert(AsyncOrder order);
}