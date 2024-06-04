package com.neusoft.neu24.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.user.config.JwtProperties;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.user.mapper.UserMapper;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.user.utils.JwtUtil;
import com.neusoft.neu24.user.utils.RegexUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.neusoft.neu24.config.RedisConstants.*;


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

    @Resource
    StringRedisTemplate stringRedisTemplate;

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

            // 保存用户信息到Redis缓存中
            Map<String, Object> userMap = convertUserToMap(user);
            stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
            // 为Redis的数据设置与token一样的有效期
            stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());
            // 登录成功,返回用户信息
            return new HttpResponseEntity<User>().loginSuccess(user);
        } catch ( Exception e ) {
            return new HttpResponseEntity<User>().serverError(null);
        }
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 发送是否成功
     */
    @Override
    public HttpResponseEntity<Boolean> sendSMSCode(String phone) {

        // 1. 校验手机号格式是否合规
        if ( !RegexUtils.isPhoneInvalid(phone) ) {
            // 不合规，返回错误
            return new HttpResponseEntity<>(ResponseEnum.PHONE_INVALID, null);
        }
        // 2. 手机号合规，生成随机的6位验证码
        String SMSCode = RandomUtil.randomNumbers(6);

        // 3. 将验证码存入Redis中，设置过期时间为 5 分钟(在RedisConstants中统一配置)
        stringRedisTemplate.opsForValue().set(LOGIN_SMS_KEY + phone, SMSCode, LOGIN_SMS_TTL, TimeUnit.MINUTES);

        // TODO 4. 通过第三方API发送验证码到手机(这里未实现，仅打印输出)
        System.out.println("验证码发送成功, 验证码为:{ " + SMSCode + " }");

        // 5. 返回成功
        return new HttpResponseEntity<Boolean>().success(true);
    }

    /**
     * 手机号登录/注册
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录/注册是否成功
     */
    @Override
    public HttpResponseEntity<User> loginByPhone(String phone, String smsCode) {

        // 1. 判断验证时手机号码是否合规
        if ( !RegexUtils.isPhoneInvalid(phone) ) {
            return new HttpResponseEntity<>(ResponseEnum.PHONE_INVALID, null);
        }

        // 2. 从Redis中获取验证码
        String code = stringRedisTemplate.opsForValue().get(LOGIN_SMS_KEY + phone);
        if ( code == null || !code.equals(smsCode) ) {
            return new HttpResponseEntity<>(ResponseEnum.SMS_CODE_ERROR, null);
        }

        // 3. 根据手机号查询用户，判断用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("telephone", phone));
        // 用户不存在，则立即创建用户，进行注册
        if ( user == null ) {
            user = registerUserByPhone(phone);
        }

        // 4. 保存用户信息到Redis中
        // 根据用户信息生成 Jwt Token
        String token = jwtUtil.createToken(user.getUserId(), jwtProperties.getTokenTTL());
        user.setToken(token);
        // 将用户对象转为Hash存储
        Map<String, Object> userMap = convertUserToMap(user);

        stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
        // 为Redis的数据设置与token一样的有效期
        stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());

        return new HttpResponseEntity<User>().success(user);
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
                // 优先查询 Redis 缓存
                Map<Object,Object> cashUserMap = stringRedisTemplate.opsForHash().entries(LOGIN_TOKEN + user.getUserId());
                if(!cashUserMap.isEmpty()) {
                    User cashUser = BeanUtil.fillBeanWithMap(cashUserMap, new User(), false);
                    return new HttpResponseEntity<User>().success(cashUser);
                }
                // 缓存中没有数据时再从数据库查询
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

    /**
     * 条件查找网格员
     *
     * @param gridManager 网格员信息
     * @return 查询结果
     */
    @Override
    public HttpResponseEntity<List<User>> selectGridManagers(User gridManager) {
        if ( gridManager == null ) {
            return new HttpResponseEntity<List<User>>().resultIsNull(null);
        } else {
            try {
                // 查询条件
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                if ( gridManager.getGmProvinceId() != null ) {
                    queryWrapper.eq("gm_province_id", gridManager.getGmProvinceId());
                }
                if ( gridManager.getGmCityId() != null ) {
                    queryWrapper.eq("gm_city_id", gridManager.getGmCityId());
                }
                if ( gridManager.getGmTownId() != null ) {
                    queryWrapper.eq("gm_town_id", gridManager.getGmTownId());
                }
                queryWrapper.eq("role_id", 2);
                List<User> gridManagers = userMapper.selectList(queryWrapper);

                // 处理查询结果
                if ( gridManagers == null || gridManagers.isEmpty() ) {
                    return new HttpResponseEntity<List<User>>().resultIsNull(null);
                } else {
                    return new HttpResponseEntity<List<User>>().success(gridManagers);
                }
            } catch ( Exception e ) {
                return new HttpResponseEntity<List<User>>().serverError(null);
            }
        }
    }

    /**
     * 根据手机号创建用户
     *
     * @param phone 手机号
     * @return 创建的用户
     */
    private User registerUserByPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setTelephone(phone);
        user.setUsername("nep_usr_" + RandomUtil.randomString(8));
        user.setRealName("未设置");
        user.setPassword("00000000");
        user.setBirthday("2000-01-01");
        user.setGender(1);
        user.setRoleId(1);

        // 保存用户
        userMapper.insert(user);
        return user;
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();

        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("password", user.getPassword());
        map.put("realName", user.getRealName());
        map.put("telephone", user.getTelephone());
        map.put("gender", toStringOrNull(user.getGender()));
        map.put("birthday", user.getBirthday());
        map.put("roleId", toStringOrNull(user.getRoleId()));
        map.put("headPhotoLoc", user.getHeadPhotoLoc());
        map.put("gmProvinceId", toStringOrNull(user.getGmProvinceId()));
        map.put("gmCityId", toStringOrNull(user.getGmCityId()));
        map.put("gmTownId", toStringOrNull(user.getGmTownId()));
        map.put("gmState", toStringOrNull(user.getGmState()));
        map.put("remarks", user.getRemarks());
        map.put("token", user.getToken());

        return map;
    }

    private String toStringOrNull(Object value) {
        return Optional.ofNullable(value).map(Object::toString).orElse(null);
    }
}
