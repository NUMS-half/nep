package com.neusoft.neu24.service.impl;

import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.mapper.UserMapper;
import com.neusoft.neu24.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


/**
 * 服务实现类
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 验证用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户ID
     */
    @Override
    public String login(String username, String password) {
        return userMapper.login(username, password);
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @return 是否注册成功
     */
    @Override
    public boolean register(User user) throws Exception {
        return userMapper.insert(user) != 0;
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateUser(User user) throws Exception {
        return userMapper.updateById(user) != 0;
    }


}
