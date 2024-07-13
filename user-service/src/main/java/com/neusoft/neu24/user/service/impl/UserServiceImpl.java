package com.neusoft.neu24.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.client.RoleClient;
import com.neusoft.neu24.entity.*;
import com.neusoft.neu24.dto.UserDTO;
import com.neusoft.neu24.exceptions.*;
import com.neusoft.neu24.user.config.JwtProperties;
import com.neusoft.neu24.user.mapper.UserMapper;
import com.neusoft.neu24.user.service.IUserService;
import com.neusoft.neu24.user.utils.HttpUtils;
import com.neusoft.neu24.user.utils.JwtUtil;
import com.neusoft.neu24.user.utils.RegexUtils;
import io.lettuce.core.RedisException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.neusoft.neu24.config.RedisConstants.*;


/**
 * 用户服务实现类
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;

    /**
     * JWT配置属性
     */
    private final JwtProperties jwtProperties;

    /**
     * 用户数据访问接口
     */
    @Resource
    private UserMapper userMapper;

    /**
     * Redis操作模板
     */
    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * RabbitMQ消息发送模板
     */
    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 角色服务客户端(由动态代理注入)
     */
    private final RoleClient roleClient;

    /**
     * 验证用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<UserDTO> login(String username, String password) throws AuthException {
        // 1. 判断用户名和密码是否为空/空串
        if ( StringUtils.isEmpty(username) || StringUtils.isEmpty(password) ) {
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.LOGIN_CONTENT_IS_NULL);
        }
        try {
            // 2. 按照账号查询用户信息
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            User user = userMapper.selectOne(queryWrapper.eq(User::getUsername, username));
            if ( user != null ) {
                // 2.1 判断用户状态
                Integer status = user.getStatus();
                // 用户状态为禁用，返回403
                if ( status == 0 ) {
                    return new HttpResponseEntity<UserDTO>().error(ResponseEnum.FORBIDDEN);
                }
                // 用户状态为已删除，返回300
                if ( status == -1 ) {
                    return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.USER_NOT_EXIST);
                }
            }
            // 2.2 验证用户是否存在，且用户存在时验证密码是否正确
            if ( user == null || !user.getPassword().equals(SecureUtil.md5(password)) ) {
                logger.info("用户 {} 登录失败: 因为用户不存在或密码不正确", username);
                return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.LOGIN_FAIL);
            }
            // 3. 登录成功，处理缓存等逻辑
            return handleLogin(user);
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("用户 {} 登录时发生异常: {}", username, e.getMessage(), e);
            throw new AuthException(e.getMessage(), e);
        }
    }

    /**
     * 用户注销业务
     *
     * @param userId 用户ID
     * @return 注销是否成功
     */
    @Override
    public HttpResponseEntity<Boolean> logout(String userId) {
        // 1. 判断用户ID是否为空
        if ( StringUtils.isEmpty(userId) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 判断用户是否已经注销
        if ( Boolean.FALSE.equals(stringRedisTemplate.hasKey(LOGIN_TOKEN + userId)) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.HAS_LOGOUT);
        }
        try {
            // 3. 删除Redis中的用户信息
            stringRedisTemplate.delete(LOGIN_TOKEN + userId);
            logger.info("用户 {} 注销成功", userId);
            return new HttpResponseEntity<Boolean>().success(true);
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("注销用户时发生异常: {}, 用户 {} 注销失败", e.getMessage(), userId);
            throw new AuthException("注销用户时发生异常", e);
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
        if ( !RegexUtils.isPhoneValid(phone) ) {
            // 不合规，返回错误
            return new HttpResponseEntity<>().fail(ResponseEnum.PHONE_INVALID);
        }
        // 2. 手机号合规，生成随机的6位验证码
        String smsCode = RandomUtil.randomNumbers(6);

        // 3. 将验证码存入Redis中，设置过期时间为 5 分钟(在RedisConstants中统一配置)
        stringRedisTemplate.opsForValue().set(LOGIN_SMS_KEY + phone, smsCode, LOGIN_SMS_TTL, TimeUnit.MINUTES);

        // 4. 通过第三方API发送验证码到手机
        logger.info("为 {} 发送验证码成功, 验证码为:{}", phone, smsCode); // 未接入AQI时，控制台打印验证码代替发送
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "01600dfa8a514d3bac819f016523b4a7";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + smsCode + "**minute**:5");

        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodies = new HashMap<>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodies);
            // 5. 返回成功
            logger.info("调用第三方验证码接口，返回值为: {}", response);
            return new HttpResponseEntity<>().success(response.getStatusLine().getStatusCode());
        } catch ( Exception e ) {
            logger.error("发送验证码失败: {}", e.getMessage(), e);
            // 6. 异常处理
            throw new RuntimeException(e);
        }

    }

    /**
     * 手机号登录/注册
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录/注册是否成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<UserDTO> loginByPhone(String phone, String smsCode) throws SaveException, RedisException {

        // 1. 判断验证时手机号码是否合规
        if ( !RegexUtils.isPhoneValid(phone) ) {
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.PHONE_INVALID);
        }

        // 2. 从Redis中获取验证码
        String code = stringRedisTemplate.opsForValue().get(LOGIN_SMS_KEY + phone);
        if ( code == null || !code.equals(smsCode) ) {
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.SMS_CODE_ERROR);
        }

        // 3. 根据手机号查询用户，判断用户是否存在
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("telephone", phone));
        if ( user != null ) {
            Integer status = user.getStatus();
            if ( status == 0 ) {
                return new HttpResponseEntity<UserDTO>().error(ResponseEnum.FORBIDDEN);
            } else if ( status == -1 ) {
                return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.USER_NOT_EXIST);
            }
        }

        try {
            // 4. 用户不存在，则立即创建用户，进行注册
            if ( user == null ) {
                logger.info("用户 {} 不存在，进行默认注册", phone);
                user = registerUserByPhone(phone);
                if ( user == null ) {
                    return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.REGISTER_FAIL);
                }
            }
            // 5. 登录成功 / 注册成功
            logger.info("用户 {} 手机登录/注册成功", phone);
            return handleLogin(user);
        } catch ( RedisException e ) {
            logger.error("手机登录/注册时Redis数据库发生异常: {}", e.getMessage(), e);
            throw new RedisException(e);
        } catch ( Exception e ) {
            logger.error("手机登录/注册时发生异常: {}", e.getMessage(), e);
            throw new SaveException("手机登录/注册时发生异常", e);
        }
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
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<User>> selectUserByPage(User user, long current, long size) throws QueryException {
        try {
            // 1. 构建查询条件
            IPage<User> page = new Page<>(current, size);
            IPage<User> pages;
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ne(User::getStatus, -1); // 状态不为已删除
            if ( user != null ) {
                // 1.1 根据用户角色ID查询
                queryWrapper.eq(user.getRoleId() != null, User::getRoleId, user.getRoleId());
                // 1.2 若用户为网格员，还可根据省市区网格编码与工作状态查询
                if ( user.getRoleId() != null && user.getRoleId() == 2 ) {
                    queryWrapper.eq(StringUtils.isNotBlank(user.getGmProvinceCode()), User::getGmProvinceCode, user.getGmProvinceCode())
                            .eq(StringUtils.isNotBlank(user.getGmCityCode()), User::getGmCityCode, user.getGmCityCode())
                            .eq(StringUtils.isNotBlank(user.getGmTownCode()), User::getGmTownCode, user.getGmTownCode())
                            .eq(user.getGmState() != null, User::getGmState, user.getGmState());
                }
                // 1.3 根据输入的一条用户名/真实姓名/手机号模糊查询
                boolean conditionUsername = StringUtils.isNotBlank(user.getUsername());
                boolean conditionRealName = StringUtils.isNotBlank(user.getRealName());
                boolean conditionTelephone = StringUtils.isNotBlank(user.getTelephone());
                queryWrapper.and(conditionUsername && conditionRealName && conditionTelephone, wrapper -> wrapper
                        .like(conditionUsername, User::getUsername, user.getUsername())
                        .or()
                        .like(conditionRealName, User::getRealName, user.getRealName())
                        .or()
                        .like(conditionTelephone, User::getTelephone, user.getTelephone()));
            }
            // 2. 分页查询
            pages = getBaseMapper().selectPage(page, queryWrapper);
            // 3. 返回查询结果
            if ( pages == null || pages.getTotal() == 0 ) {
                return new HttpResponseEntity<IPage<User>>().resultIsNull(null);
            } else {
                logger.info("分页查询用户信息成功");
                return new HttpResponseEntity<IPage<User>>().success(pages);
            }
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("分页查询用户信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("分页查询用户信息时发生异常", e);
        }
    }

    /**
     * 根据用户ID批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<User>> selectBatchByIds(List<String> userIds) {
        // 1. 判断用户ID列表是否为空
        if ( userIds == null || userIds.isEmpty() ) {
            return new HttpResponseEntity<List<User>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 根据用户ID批量查询用户信息
            List<User> users = userMapper.selectBatchIds(userIds);
            if ( users.isEmpty() ) {
                return new HttpResponseEntity<List<User>>().resultIsNull(null);
            }
            logger.info("根据用户ID: {} 批量查询用户信息成功", userIds);
            return new HttpResponseEntity<List<User>>().success(users);
        } catch ( Exception e ) {
            logger.error("根据用户ID批量查询用户信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("根据用户ID批量查询用户信息时发生异常", e);
        }
    }

    /**
     * 修改用户状态
     *
     * @param user   用户信息
     * @param status 用户状态
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<Boolean> changeStatus(User user, Integer status) throws UpdateException {
        // 1. 判断用户信息和状态是否为空
        if ( user == null || StringUtils.isEmpty(user.getUserId()) || status == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 判断状态是否合规(-1, 0, 1)
        if ( status < -1 || status > 1 ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATE_INVALID);
        }
        try {
            // 3. 修改用户状态
            if ( userMapper.updateStatus(user.getUserId(), status) > 0 ) {
                logger.info("修改用户 {} 状态成功", user.getUserId());
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("修改用户 {} 状态失败", user.getUserId());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            logger.error("修改用户状态时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("修改用户状态异常", e);
        }
    }

    /**
     * 修改用户网格员工作状态
     *
     * @param gmUserId 网格员ID
     * @param gmState  修改为的状态
     * @return 修改是否成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<Boolean> changeGmState(String gmUserId, Integer gmState) throws UpdateException {
        // 1. 判断网格员ID和状态是否为空
        if ( StringUtils.isEmpty(gmUserId) || gmState == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 判断网格员工作状态是否合规(0, 1, 2, 3)
        if ( gmState < 0 || gmState > 3 ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATE_INVALID);
        }
        try {
            // 3. 修改网格员工作状态
            if ( userMapper.updateGmState(gmUserId, gmState) != 0 ) {
                logger.info("修改网格员 {} 工作状态为 {} 成功", gmUserId, gmState);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("修改网格员 {} 工作状态为 {} 失败", gmUserId, gmState);
                throw new UpdateException("修改网格员工作状态失败");
            }
        } catch ( Exception e ) {
            logger.error("修改网格员工作状态时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("修改网格员工作状态时发生异常", e);
        }
    }

    /**
     * 删除用户
     *
     * @param user 用户信息
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<Boolean> deleteUser(User user) throws UpdateException {
        // 1. 判断用户信息是否为空
        if ( user == null || StringUtils.isEmpty(user.getUserId()) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 逻辑删除用户
            if ( userMapper.updateStatus(user.getUserId(), -1) > 0 ) {
                logger.info("(逻辑)删除用户 {} 成功", user.getUserId());
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("(逻辑)删除用户 {} 失败", user.getUserId());
                return new HttpResponseEntity<>(ResponseEnum.DELETE_FAIL, false);
            }
        } catch ( Exception e ) {
            logger.error("删除用户时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("删除用户时发生异常", e);
        }
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 是否注册成功
     */
    @Override
    @Transactional
    public HttpResponseEntity<UserDTO> register(User user) throws SaveException {
        // 1. 校验手机号合规性
        if ( !RegexUtils.isPhoneValid(user.getTelephone()) ) {
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.PHONE_INVALID);
        }
        // 2. 校验用户名/手机号是否已经使用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
                .or()
                .eq(User::getTelephone, user.getTelephone());
        User existUser = userMapper.selectOne(wrapper);
        if ( existUser != null ) {
            // 2.1 用户名已被使用
            if ( existUser.getUsername().equals(user.getUsername()) ) {
                return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.USERNAME_HAS_USED);
            }
            // 2.2 手机号已被使用
            if ( existUser.getTelephone().equals(user.getTelephone()) ) {
                return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.PHONE_HAS_USED);
            }
        }
        // 3. 对用户密码进行MD5加密
        user.setPassword(SecureUtil.md5(user.getPassword()));
        try {
            user.setStatus(1); // 用户只能注册为网格员
            // 4. 插入用户信息
            if ( userMapper.insert(user) > 0 ) {
                logger.info("用户 {} 注册成功", user.getUserId());
                return new HttpResponseEntity<UserDTO>().success(new UserDTO(user));
            } else {
                logger.info("用户 {} 注册失败", user.getUserId());
                return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.REGISTER_FAIL);
            }
        } catch ( DataAccessException e ) {
            logger.warn("因为输入不符合数据库约束，用户 {} 注册失败", user.getUserId());
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.REGISTER_FAIL);
        } catch ( Exception e ) {
            logger.error("注册用户时发生异常: {}", e.getMessage(), e);
            throw new SaveException("注册用户时发生异常", e);
        }
    }

    /**
     * 管理员添加用户信息
     *
     * @param user 用户信息
     * @return 是否添加成功
     */
    @Override
    public HttpResponseEntity<Boolean> addUser(User user) {
        // 校验手机号合规性
        if ( !RegexUtils.isPhoneValid(user.getTelephone()) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.PHONE_INVALID);
        }
        // 2. 校验用户名/手机号是否已经使用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
                .or()
                .eq(User::getTelephone, user.getTelephone());
        User existUser = userMapper.selectOne(wrapper);
        if ( existUser != null ) {
            // 2.1 用户名已被使用
            if ( existUser.getUsername().equals(user.getUsername()) ) {
                logger.info("用户名 {} 已被使用", user.getUsername());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.USERNAME_HAS_USED);
            }
            // 2.2 手机号已被使用
            if ( existUser.getTelephone().equals(user.getTelephone()) ) {
                logger.info("手机号 {} 已被使用", user.getTelephone());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.PHONE_HAS_USED);
            }
        }
        // 3. 对用户密码进行MD5加密
        user.setPassword(SecureUtil.md5(user.getPassword()));
        try {
            // 4. 插入用户信息
            if ( userMapper.insert(user) > 0 ) {
                logger.info("用户 {} 添加成功", user.getUserId());
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("用户 {} 添加失败", user.getUserId());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ADD_FAIL);
            }
        } catch ( DataAccessException e ) {
            logger.warn("因为输入不符合数据库约束，用户 {} 添加失败", user.getUserId());
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ADD_FAIL);
        } catch ( Exception e ) {
            logger.error("添加用户时发生异常: {}", e.getMessage(), e);
            throw new SaveException("添加用户时发生异常", e);
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否更新成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateUser(User user) throws UpdateException, RedisException {
        // 1. 待更新用户信息/ID不能为空
        if ( user == null || StringUtils.isEmpty(user.getUserId()) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 获取原本的用户，校验用户是否存在
        User oldUser;
        // 2.1 先从缓存中查询用户信息
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_TOKEN + user.getUserId());
        // 2.2 缓存中没有数据时，再从数据库查询
        if ( userMap.isEmpty() ) {
            oldUser = userMapper.selectById(user.getUserId());
        } else {
            oldUser = BeanUtil.fillBeanWithMap(userMap, new User(), false);
        }
        // 3. 校验用户是否存在
        if ( oldUser == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.USER_NOT_EXIST);
        }
        // 4. 用户存在时，更新用户数据
        try {
            // 4.1 当用户修改密码时
            if ( StringUtils.isNotBlank(user.getPassword()) ) {
                // 先对新密码进行MD5加密
                String newPassword = SecureUtil.md5(user.getPassword());
                // 判断新密码是否与旧密码相同
                if ( newPassword.equals(oldUser.getPassword()) ) {
                    return new HttpResponseEntity<Boolean>().fail(ResponseEnum.PASSWORD_SAME);
                }
                if ( !user.getPassword().equals(oldUser.getPassword()) ) {
                    user.setPassword(newPassword);
                }
            }
            // 4.2 更新用户信息
            if ( userMapper.updateById(user) > 0 ) {
                // 更新缓存中的用户信息
                refreshUserCache(user);
                // 当角色发生变化时，通知前端重新获取角色以及权限
                if ( !user.getRoleId().equals(oldUser.getRoleId()) ) {
                    logger.info("用户 {} 的角色发生变化，通知前端重新获取角色以及权限", user.getUserId());
                    rabbitTemplate.convertAndSend("role.exchange", "role.change." + user.getUserId(), user);
                }
                logger.info("更新用户 {} 信息成功", user.getUserId());
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.info("更新用户 {} 信息失败", user.getUserId());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( DataAccessException e ) {
            logger.warn("因为输入不符合数据库约束，用户 {} 更新失败", user.getUserId());
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( RedisException e ) {
            logger.error("更新用户信息时Redis数据库发生异常: {}", e.getMessage(), e);
            throw new RedisException(e);
        } catch ( Exception e ) {
            logger.error("更新用户信息时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("更新用户信息时发生异常", e);
        }
    }

    /**
     * 获取所有用户信息列表
     *
     * @return 用户信息列表
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<User>> selectAllUser() throws QueryException {
        try {
            List<User> users = userMapper.selectList(new QueryWrapper<User>().ne("status", -1));
            if ( CollUtil.isEmpty(users) ) {
                return new HttpResponseEntity<List<User>>().resultIsNull(null);
            } else {
                logger.info("查询所有用户信息成功");
                return new HttpResponseEntity<List<User>>().success(users);
            }
        } catch ( Exception e ) {
            logger.error("查询所有用户信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询所有用户信息时发生异常", e);
        }
    }

    /**
     * 查询用户信息
     *
     * @param user 用户信息
     * @return 查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<User> selectUser(User user) throws QueryException {
        if ( user == null ) {
            return new HttpResponseEntity<User>().resultIsNull(null);
        } else {
            try {
                // 优先查询 Redis 缓存
                Map<Object, Object> cashUserMap = stringRedisTemplate.opsForHash().entries(LOGIN_TOKEN + user.getUserId());
                // 缓存中有数据时，直接返回未删除的角色
                if ( !cashUserMap.isEmpty() ) {
                    User cashUser = BeanUtil.fillBeanWithMap(cashUserMap, new User(), false);
                    if ( cashUser.getStatus() != -1 ) {
                        return new HttpResponseEntity<User>().success(cashUser);
                    }
                }
                // 缓存中没有数据时再从数据库查询
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                // 可根据用户ID、用户名、手机号查询用户信息
                queryWrapper.eq("user_id", user.getUserId())
                        .or()
                        .eq("username", user.getUsername())
                        .or()
                        .eq("telephone", user.getTelephone())
                        .ne("status", -1);
                User resultUser = userMapper.selectOne(queryWrapper);
                if ( resultUser == null ) {
                    return new HttpResponseEntity<User>().resultIsNull(null);
                } else {
                    return new HttpResponseEntity<User>().success(resultUser);
                }
            } catch ( Exception e ) {
                logger.error("查询指定用户信息时发生异常: {}", e.getMessage(), e);
                throw new QueryException("查询指定用户信息时发生异常", e);
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
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<UserDTO>> selectGridManagers(User gridManager) {
        // 1. 判断网格员信息是否为空
        if ( gridManager == null ) {
            return new HttpResponseEntity<List<UserDTO>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    // 用户名条件
                    .eq(StringUtils.isNotBlank(gridManager.getUsername()), User::getUsername, gridManager.getUsername())
                    // 电话号码条件
                    .eq(StringUtils.isNotBlank(gridManager.getTelephone()) && RegexUtils.isPhoneValid(gridManager.getTelephone()),
                            User::getTelephone, gridManager.getTelephone())
                    // 省份网格条件
                    .eq(StringUtils.isNotBlank(gridManager.getGmProvinceCode()), User::getGmProvinceCode, gridManager.getGmProvinceCode())
                    // 城市网格条件
                    .eq(StringUtils.isNotBlank(gridManager.getGmCityCode()), User::getGmCityCode, gridManager.getGmCityCode())
                    // 区/县网格条件
                    .eq(StringUtils.isNotBlank(gridManager.getGmTownCode()), User::getGmTownCode, gridManager.getGmTownCode())
                    // 网格员工作状态条件
                    .eq(gridManager.getGmState() != null, User::getGmState, gridManager.getGmState())
                    // 角色ID为网格员
                    .eq(User::getRoleId, 2)
                    // 状态不为已删除
                    .ne(User::getStatus, -1);
            List<User> gridManagers = userMapper.selectList(queryWrapper);

            // 3. 处理查询结果
            if ( gridManagers == null || gridManagers.isEmpty() ) {
                return new HttpResponseEntity<List<UserDTO>>().resultIsNull(null);
            } else {
                List<UserDTO> gmManagerDTOS = new ArrayList<>();
                gridManagers.forEach(user -> gmManagerDTOS.add(new UserDTO(user)));
                logger.info("条件查找网格员成功");
                return new HttpResponseEntity<List<UserDTO>>().success(gmManagerDTOS);
            }
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("条件查找网格员时发生异常: {}", e.getMessage(), e);
            throw new QueryException("条件查找网格员时发生异常", e);
        }
    }

    /**
     * 根据手机号创建用户
     *
     * @param phone 手机号
     * @return 创建的用户
     */
    private User registerUserByPhone(String phone) throws SaveException {
        // 创建用户
        User user = new User();
        user.setTelephone(phone);
        // 用户手机注册时的默认信息
        user.setUsername("nep_" + RandomUtil.randomString(8)); // 随机生成初始用户名
        user.setRealName("NEP公众监督员");
        user.setPassword(SecureUtil.md5("nep123456")); // 默认密码为: nep123456
        user.setBirthday("2000-01-01");
        user.setGender(1); // 默认性别为男
        user.setRoleId(1); // 默认角色为公众监督员
        user.setStatus(1); // 默认状态为启用

        try {
            // 保存手机注册的默认用户信息
            return userMapper.insert(user) == 0 ? null : user;
        } catch ( DataAccessException e ) {
            return null;
        } catch ( Exception e ) {
            throw new SaveException("手机注册时发生异常", e);
        }
    }

    /**
     * 处理用户登录校验通过后的逻辑
     *
     * @param user 用户信息
     * @return 登录成功后的响应
     */
    public HttpResponseEntity<UserDTO> handleLogin(User user) throws RedisException {
        try {
            // 登录校验通过后：
            // 1. 生成Token
            user.setToken(jwtUtil.createToken(user, jwtProperties.getTokenTTL()));
            // 2. 保存用户信息到Redis缓存中
            Map<String, Object> userMap = convertUserToMap(user);
            logger.info("用户 {} 登录成功，开始缓存用户数据", userMap);

            HttpResponseEntity<List<Integer>> response = roleClient.selectSystemNodeById(user.getRoleId());
            if ( response.getCode() == 200 ) {
                UserDTO userDTO = new UserDTO(user);
                userDTO.setPermissions(response.getData());
                userMap.put("permissions", response.getData());
                stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
                // 3. 为Redis的数据设置与token一样的有效期
                stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());
                // 4. 返回登录成功的响应
                return new HttpResponseEntity<UserDTO>().success(ResponseEnum.LOGIN_SUCCESS, userDTO);
            }
            stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
            stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());
            logger.info("用户 {} 数据缓存成功", userMap);
            return new HttpResponseEntity<UserDTO>().fail(ResponseEnum.PERMISSION_FAIL, new UserDTO(user));
        } catch ( Exception e ) {
            throw new RedisException("登录成功后，缓存用户数据处理发生异常", e);
        }
    }

    /**
     * 更新用户信息后，刷新Redis缓存数据
     *
     * @param user 用户信息
     */
    private void refreshUserCache(@NotNull User user) throws RedisException {
        try {
            if ( Boolean.FALSE.equals(stringRedisTemplate.hasKey(LOGIN_TOKEN + user.getUserId())) ) {
                return;
            }
            user.setToken(jwtUtil.createToken(user, jwtProperties.getTokenTTL()));
            Map<String, Object> userMap = convertUserToMap(user);
            stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN + user.getUserId(), userMap);
            stringRedisTemplate.expire(LOGIN_TOKEN + user.getUserId(), jwtProperties.getTokenTTL());
        } catch ( Exception e ) {
            throw new RedisException("更新用户信息后，刷新缓存数据发生异常", e);
        }
    }

    /**
     * 将User对象转换为Map
     *
     * @param user 用户对象
     * @return Map对象
     */
    private Map<String, Object> convertUserToMap(@NotNull User user) {
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

    /**
     * 将对象转换为字符串，如果对象为null则返回null
     *
     * @param value 对象
     * @return 字符串
     */
    private String toStringOrNull(Object value) {
        return Optional.ofNullable(value).map(Object::toString).orElse(null);
    }
}
