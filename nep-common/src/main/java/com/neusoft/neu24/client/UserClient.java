package com.neusoft.neu24.client;

import com.neusoft.neu24.client.fallback.UserClientFallbackFactory;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    /**
     * 查询用户信息
     * @param userInfo 用户信息
     * @return 查找到的用户信息
     */
    @PostMapping("/user/select")
    HttpResponseEntity<User> selectUser(@RequestBody Map<String, Object> userInfo);


    /**
     * <b>条件查找网格员<b/>
     *
     * @param gmInfo 网格员信息
     * @return 查询结果
     */
    @PostMapping(value = "/select/gm", headers = "Accept=application/json")
    HttpResponseEntity<List<User>> selectGridManagers(@RequestBody Map<String, Object> gmInfo);
}
