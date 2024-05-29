package com.neusoft.neu24.controller;


import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<HttpResponseEntity> login(String username, String password) {
        try {
            String userId = userService.login(username, password);
            if (userId != null) {
                return new HttpResponseEntity().get(userId).toResponseEntity();
            } else {
                return HttpResponseEntity.LOGIN_FAIL.toResponseEntity();
            }
        } catch (Exception e) {
            return HttpResponseEntity.ERROR.toResponseEntity();
        }
    }

}

