package com.ipproxy.overseas.customer.mapper;

import com.ipproxy.overseas.customer.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    Account selectByEmail(@Param("email") String email);

    int countByEmail(@Param("email") String email);

    int insertAccount(Account account);
}
