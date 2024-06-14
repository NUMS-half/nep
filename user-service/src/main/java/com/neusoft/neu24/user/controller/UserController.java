package com.neusoft.neu24.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.UserDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.user.config.UserProperties;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.utils.UserContext;
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
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    // 配置热更新的注入
    @Resource
    private UserProperties userProperties;

    /**
     * <b>用户登录校验<b/>
     *
     * @param loginInfo 登录信息
     * @return 登录校验结果
     */
    @PostMapping("/login")
    public HttpResponseEntity<UserDTO> login(@RequestBody Map<String, Object> loginInfo) {
        // TODO 未完成, 限制用户最大登录失败次数
        System.out.println(userProperties.getLoginMaxTimes());

        // 解析前端请求的用户数据
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");
        // 登录校验
        return userService.login(username, password);
    }

    /**
     * <b>发送短信验证码<b/>
     *
     * @param phone 手机号
     * @return 验证发是否发送成功
     */
    @PostMapping("/sendSMSCode")
    public HttpResponseEntity<Object> sendSMSCode(@RequestParam("phone") String phone) {
        try {
            // 发送短信验证码
            return userService.sendSMSCode(phone);
        } catch ( Exception e ) {
            return new HttpResponseEntity<>().serverError(null);
        }
    }

    /**
     * <b>手机号登录/注册<b/>
     *
     * @param loginInfo 登录信息
     * @return 登录/注册结果
     */
    @PostMapping("/login/phone")
    public HttpResponseEntity<UserDTO> loginByPhone(@RequestBody Map<String, Object> loginInfo) {
        // 解析前端请求的用户数据
        String phone = (String) loginInfo.get("phone");
        String smsCode = (String) loginInfo.get("smsCode");
        // 登录校验
        return userService.loginByPhone(phone, smsCode);
    }

    /**
     * <b>完整用户信息注册<b/>
     *
     * @param registerInfo 待注册的用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public HttpResponseEntity<UserDTO> register(@RequestBody Map<String, Object> registerInfo) {
        // 封装用户信息
        User user = BeanUtil.fillBeanWithMap(registerInfo, new User(), false);
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
     * <b>分页查询用户信息<b/>
     *
     * @param map
     * @param current
     * @param size
     * @return
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<User>> selectUserByPage(@RequestBody(required = false) Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {
        try {
            if ( map == null || map.isEmpty() ) {
                return userService.selectUserByPage(null, current, size);
            } else {
                User user = BeanUtil.fillBeanWithMap(map, new User(), false);
                return userService.selectUserByPage(user, current, size);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<IPage<User>>().serverError(null);
        }
    }

    /**
     * <b>查询用户信息<b/>
     *
     * @param userInfo 用户信息
     * @return 查询结果
     */
    @PostMapping(value = "/select", headers = "Accept=application/json")
    public HttpResponseEntity<User> selectUser(@RequestBody Map<String, Object> userInfo) {
        System.out.println(UserContext.getUser());
        // 封装用户信息
        User user = new User();
        user.setUserId((String) userInfo.get("userId"));
        user.setUsername((String) userInfo.get("username"));

        // 查询用户数据
        return userService.selectUser(user);
    }

    /**
     * <b>条件查找网格员<b/>
     *
     * @param gmInfo 网格员信息
     * @return 查询结果
     */
    @PostMapping(value = "/select/gm", headers = "Accept=application/json")
    public HttpResponseEntity<List<User>> selectGridManagers(@RequestBody Map<String, Object> gmInfo) {
        // 封装用户信息
        User user = BeanUtil.fillBeanWithMap(gmInfo, new User(), false);
        // 查询网格员信息
        return userService.selectGridManagers(user);
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
        User user = BeanUtil.fillBeanWithMap(userInfo, new User(), false);
        // 更新用户信息
        return userService.updateUser(user);
    }

    /**
     * <b>修改用户状态<b/>
     *
     * @param user   用户信息
     * @param status 用户状态
     * @return 修改结果
     */
    @PutMapping(value = "/update/status", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateUserStatus(@RequestBody User user, @RequestParam("status") int status) {
        // 更新用户状态
        return userService.changeStatus(user, status);
    }

    /**
     * <b>删除用户信息<b/>
     *
     * @param user 用户信息
     * @return 删除结果
     */
    @PutMapping(value = "/delete", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> deleteUser(@RequestBody User user) {
        // 删除用户信息
        return userService.deleteUser(user);
    }

}

