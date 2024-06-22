package com.neusoft.neu24.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.UserDTO;
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
    HttpResponseEntity<UserDTO> login(String username, String password);

    /**
     * 完整信息用户注册业务
     *
     * @param user 用户信息
     * @return 注册是否成功
     */
    HttpResponseEntity<UserDTO> register(User user);

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
    HttpResponseEntity<List<UserDTO>> selectGridManagers(User gridManager);

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 发送是否成功
     */
    HttpResponseEntity<Object> sendSMSCode(String phone);

    /**
     * 手机号登录/注册
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录/注册是否成功
     */
    HttpResponseEntity<UserDTO> loginByPhone(String phone, String smsCode);

    /**
     * 分页查询
     * @param user
     * @param current
     * @param size
     * @return
     */
    HttpResponseEntity<IPage<User>> selectUserByPage(User user, long current, long size);

    /**
     * 修改用户状态
     * @param user
     * @return
     */
    HttpResponseEntity<Boolean> changeStatus(User user, Integer status);

    /**
     * 修改用户网格员工作状态
     * @param gmUserId 网格员ID
     * @param gmState 修改为的状态
     * @return 修改是否成功
     */
    HttpResponseEntity<Boolean> changeGmState(String gmUserId, Integer gmState);

    /**
     * 删除用户
     * @param user 用户信息
     * @return 删除是否成功
     */
    HttpResponseEntity<Boolean> deleteUser(User user);

}
