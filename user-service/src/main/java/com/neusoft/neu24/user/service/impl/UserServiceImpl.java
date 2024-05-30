package com.neusoft.neu24.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.user.config.JwtProperties;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.user.mapper.UserMapper;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.user.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 服务实现类
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;

    private final JwtUtil jwtUtil;

    private final JwtProperties jwtProperties;

    /**
     * 验证用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @Override
    public HttpResponseEntity<User> login(String username, String password) {
        // 判断用户名和密码是否为空
        if ( username == null || username.isEmpty() || password == null || password.isEmpty() ) {
            return HttpResponseEntity.LOGIN_CONTENT_IS_NULL;
        }
        try {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            // 验证用户是否存在
            if ( user == null ) {
                return HttpResponseEntity.LOGIN_FAIL;
            }
            // 用户存在时验证密码是否正确
            else {
                if ( !user.getPassword().equals(password) ) {
                    return HttpResponseEntity.LOGIN_FAIL;
                }
            }
            user.setToken(jwtUtil.createToken(user.getUserId(), jwtProperties.getTokenTTL()));
            // 登录成功,返回用户信息
            return new HttpResponseEntity<User>().loginSuccess(user);
        } catch ( Exception e ) {
            return new HttpResponseEntity<User>().serverError(null);
        }
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 是否注册成功
     */
    @Override
    public HttpResponseEntity<User> register(User user) {
        try {
            // 插入用户信息
            return userMapper.insert(user) != 0 ?
                    new HttpResponseEntity<User>().success(user) :
                    HttpResponseEntity.REGISTER_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.REGISTER_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<User>().serverError(null);
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否更新成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateUser(User user) {
        // 更新用户数据
        try {
            return userMapper.updateById(user) != 0 ?
                    new HttpResponseEntity<Boolean>().success(null) :
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 获取所有用户信息列表
     *
     * @return 用户信息列表
     */
    @Override
    public HttpResponseEntity<List<User>> selectAllUser() {
        try {
            List<User> users = userMapper.selectList(null);
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
     * @param user 用户信息
     * @return 查询结果
     */
    public HttpResponseEntity<User> selectUser(User user) {
        if ( user == null ) {
            return new HttpResponseEntity<User>().resultIsNull(null);
        } else {
            try {
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", user.getUserId()).or().eq("username", user.getUsername());
                List<User> users = userMapper.selectList(queryWrapper);
                if ( users == null || users.isEmpty() ) {
                    return new HttpResponseEntity<User>().resultIsNull(null);
                } else {
                    return new HttpResponseEntity<User>().success(users.get(0));
                }
            } catch ( Exception e ) {
                return new HttpResponseEntity<User>().serverError(null);
            }
        }
    }
}
