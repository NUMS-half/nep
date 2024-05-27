package com.neusoft.neu24.nepcommon.service;

import com.neusoft.neu24.nepcommon.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.dao.DataAccessException;

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
    String login(String username, String password);

    /**
     * 用户注册业务
     *
     * @param user 用户信息
     * @return 注册是否成功
     */
    boolean register(User user) throws DataAccessException, Exception;

    /**
     * 更新用户信息业务
     *
     * @param user 用户信息
     * @return 更新是否成功
     */
    boolean updateUser(User user) throws DataAccessException, Exception;

    /**
     * 获取所有用户信息列表
     *
     * @return 用户信息列表
     */
    List<User> selectUser(User user);
}
