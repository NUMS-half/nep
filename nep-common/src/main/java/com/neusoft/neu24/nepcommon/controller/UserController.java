package com.neusoft.neu24.nepcommon.controller;


import com.neusoft.neu24.nepcommon.entity.HttpResponseEntity;
import com.neusoft.neu24.nepcommon.entity.User;
import com.neusoft.neu24.nepcommon.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 用户登录校验
     * @param username 用户名
     * @param password 密码
     * @return 登录校验结果
     */
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

    /**
     * 用户注册
     * @param user 待注册的用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<HttpResponseEntity> register(User user) {
        try {
            return userService.register(user) ? HttpResponseEntity.REGISTER_SUCCESS.toResponseEntity() : HttpResponseEntity.REGISTER_FAIL.toResponseEntity();
        } catch (Exception e) {
            return HttpResponseEntity.ERROR.toResponseEntity();
        }
    }

    /**
     * 查询所有用户信息
     * @return 查询结果
     */
    @PostMapping("/select/all")
    public ResponseEntity<HttpResponseEntity> selectAllUser() {
        try {
            return new HttpResponseEntity().get(userService.selectUser(null)).toResponseEntity();
        } catch (Exception e) {
            return HttpResponseEntity.ERROR.toResponseEntity();
        }
    }

    /**
     * 查询用户信息
     *
     * @param user 用户信息
     * @return 查询结果
     */
    @PostMapping(value = "/select", headers = "Accept=application/json")
    public ResponseEntity<HttpResponseEntity> selectUser(@RequestBody User user) {
        try {
            return new HttpResponseEntity().get(userService.selectUser(user)).toResponseEntity();
        } catch (Exception e) {
            return HttpResponseEntity.ERROR.toResponseEntity();
        }
    }

}

