package com.neusoft.neu24.client;

import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("role-service")
public interface RoleClient {

    @GetMapping(value = "/role/select/node")
    HttpResponseEntity<List<Integer>> selectSystemNodeById(@RequestParam("roleId") Integer roleId);
}
