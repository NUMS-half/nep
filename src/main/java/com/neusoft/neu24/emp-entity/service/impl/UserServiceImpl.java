package com.neusoft.neu24.emp-entity.service.impl;

import com.neusoft.neu24.emp-entity.entity.User;
import com.neusoft.neu24.emp-entity.mapper.UserMapper;
import com.neusoft.neu24.emp-entity.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
