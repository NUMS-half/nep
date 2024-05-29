package com.neusoft.neu24.report.client;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("user-service")
public interface UserClient {

    /**
     * 查询用户信息
     * @param userInfo 用户信息
     * @return 查找到的用户信息
     */
    @PostMapping("/user/select")
    HttpResponseEntity<User> selectUser(@RequestBody Map<String, Object> userInfo);
}
