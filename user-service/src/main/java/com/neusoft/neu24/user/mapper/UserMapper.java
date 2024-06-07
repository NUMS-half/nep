package com.neusoft.neu24.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.neu24.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT user_id FROM user WHERE status <> -1 AND username = #{username} AND password = #{password}")
    String login(@Param("username") String username, @Param("password") String password);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 用户状态
     * @return 更新是否成功
     */
    @Update("UPDATE user SET status = #{status} WHERE user_id = #{userId}")
    int updateStatus(@Param("userId") String userId, @Param("status") int status);

}
