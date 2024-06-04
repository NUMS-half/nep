package com.neusoft.neu24.user.service;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IUserService extends IService<User> {

    /**
     * 用户登录业务
     *
     * @param username 用户名
     * @param password 登录密码
     * @return 登录成功返回用户ID，失败返回null
     */
    HttpResponseEntity<User> login(String username, String password);

    /**
     * 完整信息用户注册业务
     *
     * @param user 用户信息
     * @return 注册是否成功
     */
    HttpResponseEntity<User> register(User user);

    /**
     * 更新用户信息业务
     *
     * @param user 用户信息
     * @return 更新是否成功
     */
    HttpResponseEntity<Boolean> updateUser(User user);

    /**
     * 获取所有用户信息列表
     *
     * @return 用户信息列表
     */
    HttpResponseEntity<List<User>> selectAllUser();

    /**
     * 查询用户信息
     *
     * @param user 用户信息
     * @return 查询结果
     */
    HttpResponseEntity<User> selectUser(User user);

    /**
     * 条件查找网格员
     *
     * @param gridManager 网格员信息
     * @return 查询结果
     */
    HttpResponseEntity<List<User>> selectGridManagers(User gridManager);

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 发送是否成功
     */
    HttpResponseEntity<Boolean> sendSMSCode(String phone);

    /**
     * 手机号登录/注册
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录/注册是否成功
     */
    HttpResponseEntity<User> loginByPhone(String phone, String smsCode);
}
