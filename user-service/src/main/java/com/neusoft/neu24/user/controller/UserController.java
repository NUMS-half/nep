package com.neusoft.neu24.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.UserDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.exceptions.LoginException;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.user.config.UserProperties;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * <b>用户(User)控制器<b/>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private IUserService userService;

    // Nacos热更新的配置注入(测试用)
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
        logger.info("Nacos热更新配置读取用户最大登录失败次数限制：{}",userProperties.getLoginMaxTimes());
        // 解析前端请求的用户数据
        String username = (String) loginInfo.get("username");
        String password = (String) loginInfo.get("password");
//        try {
            // 登录校验
            return userService.login(username, password);
//        } catch ( LoginException e ) {
//            logger.error("用户登录校验发生异常: {}", e.getMessage());
//            return new HttpResponseEntity<UserDTO>().serverError(null);
//        }
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
            logger.error("发送短信验证码发生异常: {}", e.getMessage());
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
        try {
            // 登录校验
            return userService.loginByPhone(phone, smsCode);
        } catch ( LoginException e ) {
            logger.error("用户手机登录校验发生异常: {}", e.getMessage());
            return new HttpResponseEntity<UserDTO>().serverError(null);
        }
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
        try {
            // 注册
            return userService.register(user);
        } catch ( SaveException e ) {
            logger.error("用户注册发生异常: {}", e.getMessage());
            return new HttpResponseEntity<UserDTO>().serverError(null);
        }
    }

    /**
     * <b>查询所有用户信息<b/>
     *
     * @return 查询结果
     */
    @PostMapping("/select/all")
    public HttpResponseEntity<List<User>> selectAllUser() {
        try {
            // 直接查询所有用户信息
            return userService.selectAllUser();
        } catch ( QueryException e ) {
            logger.error("查询所有用户信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<List<User>>().serverError(null);
        }
    }

    /**
     * <b>批量查询用户信息<b/>
     *
     * @param userIds 用户ID列表
     * @return 查询结果
     */
    @PostMapping(value = "/select/batch", headers= "Accept=application/json")
    public HttpResponseEntity<List<User>> selectBatchUser(@RequestBody List<String> userIds) {
        try {
            return userService.selectBatchByIds(userIds);
        } catch ( QueryException e ) {
            logger.error("批量查询指定用户信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<List<User>>().serverError(null);
        }
    }

    /**
     * <b>分页查询用户信息<b/>
     *
     * @param map 查询条件
     * @param current 当前页
     * @param size 每页显示数量
     * @return 查询结果
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
        } catch ( QueryException e ) {
            logger.error("分页查询用户信息发生异常: {}", e.getMessage());
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
        logger.info("【测试】查询用户信息时，从UserContext中取出userId: {}", UserContext.getUser());
        // 封装用户信息
        User user = new User();
        user.setUserId((String) userInfo.get("userId"));
        user.setUsername((String) userInfo.get("username"));

        try {
            // 查询用户数据
            return userService.selectUser(user);
        } catch ( QueryException e ) {
            logger.error("查询指定用户信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<User>().serverError(null);
        }
    }

    /**
     * <b>条件查找网格员<b/>
     *
     * @param gmInfo 网格员信息
     * @return 查询结果
     */
    @PostMapping(value = "/select/gm", headers = "Accept=application/json")
    public HttpResponseEntity<List<UserDTO>> selectGridManagers(@RequestBody Map<String, Object> gmInfo) {
        // 封装用户信息
        User user = BeanUtil.fillBeanWithMap(gmInfo, new User(), false);
        try {
            // 查询网格员信息
            return userService.selectGridManagers(user);
        } catch ( QueryException e ) {
            logger.error("查询网格员信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<List<UserDTO>>().serverError(null);
        }
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
        try {
            // 更新用户信息
            return userService.updateUser(user);
        } catch ( UpdateException e ) {
            logger.error("更新用户信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
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
        try {
            // 更新用户状态
            return userService.changeStatus(user, status);
        } catch ( UpdateException e ) {
            logger.error("修改用户状态发生异常: {}", e.getMessage());
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 修改公众监督员工作状态
     */
    @PutMapping(value = "/update/gm/state")
    public HttpResponseEntity<Boolean> updateGmState(@RequestParam("gmUserId") String gmUserId, @RequestParam("state") Integer state) {
        try {
            // 更新网格员工作状态
            return userService.changeGmState(gmUserId, state);
        } catch ( UpdateException e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * <b>删除用户信息<b/>
     *
     * @param user 用户信息
     * @return 删除结果
     */
    @PutMapping(value = "/delete", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> deleteUser(@RequestBody User user) {
        try {
            // 删除用户信息
            return userService.deleteUser(user);
        } catch ( UpdateException e ) {
            logger.error("删除用户信息发生异常: {}", e.getMessage());
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

}

