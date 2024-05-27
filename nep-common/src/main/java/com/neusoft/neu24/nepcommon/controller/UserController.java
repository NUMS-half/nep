package com.neusoft.neu24.nepcommon.controller;

import com.neusoft.neu24.nepcommon.entity.HttpResponseEntity;
import com.neusoft.neu24.nepcommon.entity.User;
import com.neusoft.neu24.nepcommon.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户(User)控制器
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
     *
     * @param loginInfo 登录信息
     * @return 登录校验结果
     */
    @PostMapping("/login")
    public HttpResponseEntity<String> login(@RequestBody Map<String, Object> loginInfo) {
        // 解析用户输入
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");
        // 判断用户名和密码是否为空
        if ( username == null || username.isEmpty() || password == null || password.isEmpty() ) {
            return HttpResponseEntity.LOGIN_CONTENT_IS_NULL;
        }
        try {
            // 登录校验
            return userService.login(username, password) == null ?
                    HttpResponseEntity.LOGIN_FAIL :
                    new HttpResponseEntity<String>().loginSuccess(userService.login(username, password));
        } catch ( Exception e ) {
            return new HttpResponseEntity<String>().serverError(null);
        }
    }

    /**
     * 用户注册
     *
     * @param registerInfo 待注册的用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public HttpResponseEntity<Boolean> register(@RequestBody Map<String, Object> registerInfo) {
        // 封装用户信息
        User user = mapToUser(registerInfo);
        // 注册
        try {
            return userService.register(user) ? new HttpResponseEntity<Boolean>().success(null) : HttpResponseEntity.REGISTER_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.REGISTER_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 查询所有用户信息
     *
     * @return 查询结果
     */
    @PostMapping("/select/all")
    public HttpResponseEntity<List<User>> selectAllUser() {
        try {
            // 查询所有用户信息
            List<User> users = userService.selectUser(null);
            if ( users == null || users.isEmpty() ) {
                return new HttpResponseEntity<List<User>>().resultIsNull(null);
            } else {
                return new HttpResponseEntity<List<User>>().success(users);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<User>>().serverError(null);
        }
    }

    /**
     * 查询用户信息
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
        try {
            User resultUser = userService.selectUser(user).get(0);
            return new HttpResponseEntity<User>().success(resultUser);
        } catch ( IndexOutOfBoundsException e ) {
            return new HttpResponseEntity<User>().resultIsNull(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<User>().serverError(null);
        }
    }

    /**
     * 更新用户信息
     *
     * @param userInfo 用户信息
     * @return 更新结果
     */
    @PostMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateUser(@RequestBody Map<String, Object> userInfo) {
        // 封装用户信息
        User user = mapToUser(userInfo);
        // 更新用户数据
        try {
            return userService.updateUser(user) ? new HttpResponseEntity<Boolean>().success(null) : HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

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

