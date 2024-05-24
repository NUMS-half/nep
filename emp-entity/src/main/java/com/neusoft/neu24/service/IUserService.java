package com.neusoft.neu24.service;

import com.neusoft.neu24.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IUserService extends IService<User> {

    /**
     * 用户登录业务
     * @param username 用户名
     * @param password 登录密码
     * @return 用户ID
     */
    String login(String username, String password);

    /**
     * 用户注册业务
     * @param user 用户信息
     * @return 注册是否成功
     */
    boolean register(User user) throws Exception;

    boolean updateUser(User user) throws Exception;
}
