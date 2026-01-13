package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.entity.system.LocationResponse;
import com.ipproxy.overseas.customer.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/system")
public class SystemController extends BaseController {
    @Autowired
    private SystemService systemService;

    @GetMapping("/locations")
    public ApiResponse<List<LocationResponse>> getLocations(Authentication authentication, @RequestParam String type) {
        Long uid = getCurrentUserId(authentication);
        List<LocationResponse> data = systemService.locations(type, uid);
        return ApiResponse.success(data);
    }
}