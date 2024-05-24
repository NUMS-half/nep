package com.neusoft.neu24.service;

import com.neusoft.neu24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IUserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    void login() {

    }

    @Test
    void register() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setGender(1);
        user.setRealName("张三");
        user.setBirthday("2000-01-01");
        user.setRoleId(1);
        user.setTelephone("12345678900");

        try {
            userService.register(user);
        } catch ( Exception e ) {
            System.out.println("注册失败");
        }

    }
}