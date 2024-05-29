package com.neusoft.neu24.mapper;

import com.neusoft.neu24.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 *  Mapper 接口
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 验证用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登陆是否成功
     */
    @Select("select user_id from user where username = #{username} and password = #{password}")
    String login(@Param("username") String username, @Param("password") String password);

}
