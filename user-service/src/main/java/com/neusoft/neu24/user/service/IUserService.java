package com.neusoft.neu24.user.service;

import com.neusoft.neu24.user.entity.HttpResponseEntity;
import com.neusoft.neu24.user.entity.User;
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
}
