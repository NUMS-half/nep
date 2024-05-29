package com.neusoft.neu24.user.controller;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.user.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <b>用户(User)控制器<b/>
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
     * <b>用户登录校验<b/>
     *
     * @param loginInfo 登录信息
     * @return 登录校验结果
     */
    @PostMapping("/login")
    public HttpResponseEntity<User> login(@RequestBody Map<String, Object> loginInfo) {
        // 解析前端请求的用户数据
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");
        // 登录校验
        return userService.login(username, password);
    }

    /**
     * <b>完整用户信息注册<b/>
     *
     * @param registerInfo 待注册的用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public HttpResponseEntity<User> register(@RequestBody Map<String, Object> registerInfo) {
        // 封装用户信息
        User user = mapToUser(registerInfo);
        // 注册
        return userService.register(user);
    }

    /**
     * <b>查询所有用户信息<b/>
     *
     * @return 查询结果
     */
    @PostMapping("/select/all")
    public HttpResponseEntity<List<User>> selectAllUser() {
        // 直接查询所有用户信息
        return userService.selectAllUser();
    }

    /**
     * <b>查询用户信息<b/>
     *
     * @param userInfo 用户信息
     * @return 查询结果
     */
    @PostMapping(value = "/select", headers = "Accept=application/json")
    public HttpResponseEntity<User> selectUser(@RequestBody Map<String, Object> userInfo) {
        // 封装用户信息
        User user = new User();
        user.setUserId((String) userInfo.get("userId"));
        user.setUsername((String) userInfo.get("username"));
        // 查询用户数据
        return userService.selectUser(user);
    }

    /**
     * <b>更新用户信息<b/>
     *
     * @param userInfo 用户信息
     * @return 更新结果
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateUser(@RequestBody Map<String, Object> userInfo) {
        // 封装用户信息
        User user = mapToUser(userInfo);
        // 更新用户信息
        return userService.updateUser(user);
    }

    /**
     * <b>将Map转换为User对象<b/>
     *
     * @param map 前端传来的Map对象
     * @return User对象
     */
    private User mapToUser(Map<String, Object> map) {
        User user = new User();
        map.forEach((key, value) -> {
            switch ( key ) {
                case "userId":
                    user.setUserId((String) value);
                    break;
                case "username":
                    user.setUsername((String) value);
                    break;
                case "password":
                    user.setPassword((String) value);
                    break;
                case "realName":
                    user.setRealName((String) value);
                    break;
                case "telephone":
                    user.setTelephone((String) value);
                    break;
                case "gender":
                    user.setGender((Integer) value);
                    break;
                case "birthday":
                    user.setBirthday((String) value);
                    break;
                case "roleId":
                    user.setRoleId((Integer) value);
                    break;
                case "headPhotoLoc":
                    user.setHeadPhotoLoc((String) value);
                    break;
                case "gmProvinceId":
                    user.setGmProvinceId((String) value);
                    break;
                case "gmCityId":
                    user.setGmCityId((String) value);
                    break;
                case "gmTownId":
                    user.setGmTownId((String) value);
                    break;
                case "gmState":
                    user.setGmState((Integer) value);
                    break;
                case "remarks":
                    user.setRemarks((String) value);
                    break;
                default:
                    break;
            }
        });
        return user;
    }

}

