package com.ipproxy.overseas.customer;

import com.ipproxy.overseas.customer.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IpproxyOverseasCustomerApplicationTests {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private JwtTokenService jwtTokenService;

    @Test
    void contextLoads() {
    }

    @Test
    void authMeRequiresToken() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void authMeWithTokenSuccess() throws Exception {
        String token = jwtTokenService.createAccessToken("1", "admin@example.com");
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("admin@example.com"));
    }
}
