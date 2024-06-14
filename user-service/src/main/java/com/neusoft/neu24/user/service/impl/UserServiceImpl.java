package com.neusoft.neu24.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.*;
import com.neusoft.neu24.dto.UserDTO;
import com.neusoft.neu24.user.config.JwtProperties;
import com.neusoft.neu24.user.mapper.UserMapper;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.user.utils.HttpUtils;
import com.neusoft.neu24.user.utils.JwtUtil;
import com.neusoft.neu24.user.utils.RegexUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.neusoft.neu24.config.RedisConstants.*;


/**
 * 用户服务实现类
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
    public HttpResponseEntity<UserDTO> login(String username, String password) {
        // 判断用户名和密码是否为空
        if ( username == null || username.isEmpty() || password == null || password.isEmpty() ) {
            return HttpResponseEntity.LOGIN_CONTENT_IS_NULL;
        }
        try {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if ( user != null ) {
                Integer status = user.getStatus();
                if ( status == 0 ) {
                    return new HttpResponseEntity<>(ResponseEnum.FORBIDDEN, null);
                } else if ( status == -1 ) {
                    return new HttpResponseEntity<>(ResponseEnum.USER_NOT_EXIST, null);
                }
            }
            // 验证用户是否存在，且用户存在时验证密码是否正确
            if ( user == null || !user.getPassword().equals(password) ) {
                return HttpResponseEntity.LOGIN_FAIL;
            }
            // 登录成功
            return handleLogin(user);
        } catch ( Exception e ) {
            return new HttpResponseEntity<UserDTO>().serverError(null);
        }
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 发送是否成功
     */
    @Override
    public HttpResponseEntity<Object> sendSMSCode(String phone) {

        // 1. 校验手机号格式是否合规
        if ( !RegexUtils.isPhoneInvalid(phone) ) {
            // 不合规，返回错误
            return new HttpResponseEntity<>(ResponseEnum.PHONE_INVALID, null);
        }
        // 2. 手机号合规，生成随机的6位验证码
        String smsCode = RandomUtil.randomNumbers(6);

        // 3. 将验证码存入Redis中，设置过期时间为 5 分钟(在RedisConstants中统一配置)
        stringRedisTemplate.opsForValue().set(LOGIN_SMS_KEY + phone, smsCode, LOGIN_SMS_TTL, TimeUnit.MINUTES);

        // 4. 通过第三方API发送验证码到手机
        System.out.println("验证码发送成功, 验证码为:{ " + smsCode + " }");
//        String host = "https://gyytz.market.alicloudapi.com";
//        String path = "/sms/smsSend";
//        String method = "POST";
//        String appcode = "01600dfa8a514d3bac819f016523b4a7";
//        Map<String, String> headers = new HashMap<>();
//        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);
//        Map<String, String> querys = new HashMap<>();
//        querys.put("mobile", phone);
//        querys.put("param", "**code**:" + smsCode + "**minute**:5");
//
//        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html
//        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
//        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
//        Map<String, String> bodies = new HashMap<>();
//
//        try {
//            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodies);
//            // 5. 返回成功
//            return new HttpResponseEntity<>().success(response.getStatusLine().getStatusCode());
        return new HttpResponseEntity<>().success(null);
//        } catch ( Exception e ) {
//            return new HttpResponseEntity<>().serverError(null);
//        }

    }

    /**
     * 手机号登录/注册
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录/注册是否成功
     */
    @Override
    public HttpResponseEntity<UserDTO> loginByPhone(String phone, String smsCode) {

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
        if ( user != null ) {
            Integer status = user.getStatus();
            if ( status == 0 ) {
                return new HttpResponseEntity<>(ResponseEnum.FORBIDDEN, null);
            } else if ( status == -1 ) {
                return new HttpResponseEntity<>(ResponseEnum.USER_NOT_EXIST, null);
            }
        }

        // 用户不存在，则立即创建用户，进行注册
        if ( user == null ) {
            user = registerUserByPhone(phone);
            if ( user == null ) {
                return HttpResponseEntity.REGISTER_FAIL;
            }
        }
        // 登录成功 / 注册成功
        return handleLogin(user);
    }

    /**
     * 条件分页查询用户信息
     *
     * @param user    查询条件
     * @param current 当前页
     * @param size    每页数据条数
     * @return 分页查询结果
     */
    @Override
    public HttpResponseEntity<IPage<User>> selectUserByPage(User user, long current, long size) {
        IPage<User> page = new Page<>(current, size);
        IPage<User> pages;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ( user != null ) {
            queryWrapper
                    .like(User::getUsername, user.getUsername())
                    .or()
                    .like(User::getRealName, user.getRealName())
                    .or()
                    .like(User::getTelephone, user.getTelephone());
        }
        pages = getBaseMapper().selectPage(page, queryWrapper.ne(User::getStatus, -1));
        return pages == null || pages.getTotal() == 0 ?
                new HttpResponseEntity<IPage<User>>().resultIsNull(null) :
                new HttpResponseEntity<IPage<User>>().success(pages);
    }

    /**
     * 修改用户状态
     *
     * @param user   用户信息
     * @param status 用户状态
     * @return 是否修改成功
     */
    @Override
    public HttpResponseEntity<Boolean> changeStatus(User user, Integer status) {
        try {
            return userMapper.updateStatus(user.getUserId(), status) != 0 ?
                    new HttpResponseEntity<Boolean>().success(null) :
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 删除用户
     *
     * @param user 用户信息
     * @return 是否删除成功
     */
    @Override
    public HttpResponseEntity<Boolean> deleteUser(User user) {
        try {
            return userMapper.updateStatus(user.getUserId(), -1) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    new HttpResponseEntity<>(ResponseEnum.DELETE_FAIL, false);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 是否注册成功
     */
    @Override
    public HttpResponseEntity<UserDTO> register(User user) {
        if ( !RegexUtils.isPhoneInvalid(user.getTelephone()) ) {
            return new HttpResponseEntity<>(ResponseEnum.PHONE_INVALID, null);
        }
        try {
            user.setStatus(1);
            // 插入用户信息
            if ( userMapper.insert(user) != 0 ) {
                return new HttpResponseEntity<UserDTO>().success(new UserDTO(user));
            } else {
                return HttpResponseEntity.REGISTER_FAIL;
            }
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.REGISTER_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<UserDTO>().serverError(null);
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
            if ( userMapper.updateById(user) != 0 ) {
                refreshUserCache(user);
                return new HttpResponseEntity<Boolean>().success(null);
            } else {
                return HttpResponseEntity.UPDATE_FAIL;
            }
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
            List<User> users = userMapper.selectList(new QueryWrapper<User>().ne("status", -1));
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
                Map<Object, Object> cashUserMap = stringRedisTemplate.opsForHash().entries(LOGIN_TOKEN + user.getUserId());
                if ( !cashUserMap.isEmpty() ) {
                    User cashUser = BeanUtil.fillBeanWithMap(cashUserMap, new User(), false);
                    if ( cashUser.getStatus() != -1 ) {
                        return new HttpResponseEntity<User>().success(cashUser);
                    }
                }
                // 缓存中没有数据时再从数据库查询
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", user.getUserId()).or().eq("username", user.getUsername()).ne("status", -1);
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
                if ( gridManager.getGmProvinceCode() != null ) {
                    queryWrapper.eq("gm_province_code", gridManager.getGmProvinceCode());
                }
                if ( gridManager.getGmCityCode() != null ) {
                    queryWrapper.eq("gm_city_code", gridManager.getGmCityCode());
                }
                if ( gridManager.getGmTownCode() != null ) {
                    queryWrapper.eq("gm_town_code", gridManager.getGmTownCode());
                }
                queryWrapper.eq("role_id", 2).ne("status", -1);
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
        // 用户手机注册时的默认信息
        user.setUsername("nep_" + RandomUtil.randomString(8));
        user.setRealName("未设置");
        user.setPassword("00000000");
        user.setBirthday("2000-01-01");
        user.setGender(1);
        user.setRoleId(1);
        user.setStatus(1);

        // 保存用户
        if ( userMapper.insert(user) == 0 ) {
            return null;
        } else {
            return user;
        }
    }

    /**
     * 处理用户登录校验通过后的逻辑
     *
     * @param user 用户信息
     * @return 登录成功后的响应
     */
    public HttpResponseEntity<UserDTO> handleLogin(User user) {
        try {
            // 登录校验通过后：
            // 1. 生成Token
            user.setToken(jwtUtil.createToken(user, jwtProperties.getTokenTTL()));
            // 2. 保存用户信息到Redis缓存中
            Map<String, Object> userMap = convertUserToMap(user);
            stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
            // 3. 为Redis的数据设置与token一样的有效期
            stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());

            // 4. 登录成功，返回用户信息
            return new HttpResponseEntity<UserDTO>().loginSuccess(new UserDTO(user));
        } catch ( Exception e ) {
            return new HttpResponseEntity<UserDTO>().serverError(null);
        }
    }

    private void refreshUserCache(User user) {
        Map<String, Object> userMap = convertUserToMap(user);
        stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
        stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());
    }

    /**
     * 将User对象转换为Map
     */
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
        map.put("status", toStringOrNull(user.getStatus()));
        map.put("headPhotoLoc", user.getHeadPhotoLoc());
        map.put("gmProvinceCode", user.getGmProvinceCode());
        map.put("gmCityCode", user.getGmCityCode());
        map.put("gmTownCode", user.getGmTownCode());
        map.put("gmState", toStringOrNull(user.getGmState()));
        map.put("remarks", user.getRemarks());
        map.put("token", user.getToken());

        return map;
    }

    private String toStringOrNull(Object value) {
        return Optional.ofNullable(value).map(Object::toString).orElse(null);
    }
}
