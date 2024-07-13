package com.neusoft.neu24.role.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.role.mapper.RoleMapper;
import com.neusoft.neu24.role.service.IRoleService;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.neusoft.neu24.config.RedisConstants.ROLE_PERMISSION_KEY;

/**
 * <b>角色业务层实现类</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
@Transactional
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    /**
     * 角色数据访问对象
     */
    @Resource
    private RoleMapper roleMapper;

    /**
     * RabbitMQ 消息操作工具
     */
    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * Redis 缓存操作工具
     */
    @Resource
    private RedisTemplate<String, SystemNode> redisTemplate;

    /**
     * 查询所有角色信息
     *
     * @return 所有角色信息
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Role>> selectAll() {
        try {
            // 1. 查询所有角色信息
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            queryWrapper.ne("state", -1); // 查询未删除的角色
            List<Role> list = roleMapper.selectList(queryWrapper);
            // 2. 返回查询结果
            if ( CollUtil.isEmpty(list) ) {
                logger.warn("查询所有角色信息结果为空");
                return new HttpResponseEntity<List<Role>>().resultIsNull(null);
            } else {
                logger.info("查询所有角色信息成功");
                return new HttpResponseEntity<List<Role>>().success(list);
            }
        } catch ( Exception e ) {
            // 3. 异常处理
            logger.error("查询所有角色信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询所有角色信息时发生异常", e);
        }
    }

    /**
     * 条件分页查询角色信息
     *
     * @param role    查询条件
     * @param current 当前页
     * @param size    每页大小
     * @return 角色信息分页
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<Role>> selectRoleByPage(Role role, long current, long size) {
        try {
            // 1. 构建分页查询条件
            IPage<Role> page = new Page<>(current, size);
            IPage<Role> pages;
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            if ( role != null ) {
                boolean condition = StringUtils.isNotBlank(role.getRoleName()) && StringUtils.isNotBlank(role.getRemark());
                queryWrapper.eq(role.getState() != null && role.getState() != -1, Role::getState, role.getState())
                        .and(condition, wrapper -> wrapper.like(StringUtils.isNotBlank(role.getRoleName()), Role::getRoleName, role.getRoleName())
                                .or(condition)
                                .like(StringUtils.isNotBlank(role.getRemark()), Role::getRemark, role.getRemark()));
            }
            // 2. 查询角色分页信息
            pages = getBaseMapper().selectPage(page, queryWrapper);
            // 3. 返回查询结果
            if ( pages == null || pages.getTotal() == 0 ) {
                logger.warn("条件分页查询角色信息结果为空");
                return new HttpResponseEntity<IPage<Role>>().resultIsNull(null);
            } else {
                logger.info("条件分页查询角色信息成功");
                return new HttpResponseEntity<IPage<Role>>().success(pages);
            }
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("条件分页查询角色信息时发生异常: {}", e.getMessage(), e);
            throw new QueryException("条件分页查询角色信息时发生异常", e);
        }
    }

    /**
     * 添加角色信息
     *
     * @param role 角色信息
     * @return 角色信息
     */
    @Override
    public HttpResponseEntity<Role> addRole(Role role) {
        // 1. 设置ID，保证可以正常插入自增ID
        role.setRoleId(null);
        // 2. 默认状态为启用
        role.setState(1);
        try {
            // 3. 插入角色信息
            if ( roleMapper.insert(role) > 0 ) {
                logger.info("添加角色信息: {} 成功", role.getRoleName());
                return new HttpResponseEntity<Role>().success(role);
            } else {
                logger.warn("添加角色: {} 失败", role.getRoleName());
                return new HttpResponseEntity<Role>().fail(ResponseEnum.ADD_FAIL);
            }
        } catch ( DataAccessException e ) {
            // 4. 数据输入异常处理
            logger.warn("由于输入不符合数据库约束，添加角色: {} 失败: {}", role.getRoleName(), e.getMessage());
            throw new SaveException("由于输入不符合数据库约束，添加角色失败", e);
        } catch ( Exception e ) {
            // 5. 其他异常处理
            logger.error("添加角色信息时发生异常: {}", e.getMessage(), e);
            throw new SaveException("添加角色信息时发生异常", e);
        }
    }

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 是否修改成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateRole(Role role) {
        // 1. 角色ID不能为空
        if ( role.getRoleId() == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        }
        // 2. 修改角色信息
        try {
            if ( roleMapper.updateById(role) > 0 ) {
                logger.info("修改角色信息成功, roleId: {}", role.getRoleId());
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("修改角色信息失败, roleId: {}", role.getRoleId());
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( DataAccessException e ) {
            // 3. 数据输入异常处理
            logger.warn("由于输入不符合数据库约束，修改角色信息失败: {}", e.getMessage(), e);
            throw new UpdateException("由于输入不符合数据库约束，修改角色信息失败", e);
        } catch ( Exception e ) {
            // 4. 其他异常处理
            logger.error("修改角色信息时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("修改角色信息时发生异常", e);
        }
    }

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param state  状态
     * @return 是否修改成功
     */
    @Override
    public HttpResponseEntity<Boolean> changeState(Integer roleId, Integer state) {
        // 1. 输入的角色ID和状态不能为空
        if ( roleId == null || state == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        // 2. 校验状态是否合法
        if ( state < -1 || state > 1 ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATE_INVALID);
        }
        // 3. 修改角色状态
        try {
            if ( roleMapper.updateState(roleId, state) > 0 ) {
                logger.info("修改角色状态成功, roleId: {}, state: {}", roleId, state);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("修改角色状态失败, roleId: {}, state: {}", roleId, state);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("修改角色状态时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("修改角色状态时发生异常", e);
        }
    }

    /**
     * 删除角色(逻辑)
     *
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    @Override
    public HttpResponseEntity<Boolean> deleteRole(Integer roleId) {
        // 1. 角色ID不能为空
        if ( roleId == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.DELETE_FAIL);
        }
        // 2. 逻辑删除角色
        try {
            if ( roleMapper.updateState(roleId, -1) > 0 ) {
                logger.info("删除角色成功, roleId: {}", roleId);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("删除角色失败, roleId: {}", roleId);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( Exception e ) {
            // 3. 异常处理
            logger.error("删除角色时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("删除角色时发生异常", e);
        }
    }

    /**
     * 查询启用的角色的权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表(子节点)
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Integer>> selectNodeIdsByRoleId(Integer roleId) {
        // 1. 角色ID不能为空
        if ( roleId == null ) {
            return new HttpResponseEntity<List<Integer>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 查询角色权限ID列表
            List<Integer> list = roleMapper.selectNodeIdsByRoleId(roleId);
            if ( CollUtil.isEmpty(list) ) {
                return new HttpResponseEntity<List<Integer>>().resultIsNull(null);
            }
            // 3. 返回查询结果
            logger.info("查询角色权限ID列表成功, roleId: {}", roleId);
            return new HttpResponseEntity<List<Integer>>().success(list);
        } catch ( Exception e ) {
            // 4. 异常处理
            logger.error("查询角色权限ID列表时发生异常: {}", e.getMessage(), e);
            throw new QueryException("查询角色权限ID列表时发生异常", e);
        }
    }

    /**
     * 查询角色权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<SystemNode>> selectNodesByRoleId(Integer roleId) {
        // 1. 角色ID不能为空
        if ( roleId == null ) {
            return new HttpResponseEntity<List<SystemNode>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            List<SystemNode> nodeList = getNodesByRoleId(roleId);
            if ( CollUtil.isEmpty(nodeList) ) {
                return new HttpResponseEntity<List<SystemNode>>().resultIsNull(null);
            }
            logger.info("查询角色权限列表成功, roleId: {}", roleId);
            return new HttpResponseEntity<List<SystemNode>>().success(nodeList);
        } catch ( Exception e ) {
            logger.error("查询角色权限列表时发生异常", e);
            return new HttpResponseEntity<List<SystemNode>>().serverError(null);
        }
    }

    /**
     * 修改角色权限列表
     *
     * @param roleId  角色ID
     * @param nodeIds 权限ID列表
     * @return 是否修改成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateRoleAuth(Integer roleId, List<Integer> nodeIds) {
        // 1. 角色ID和权限ID列表不能为空
        if ( roleId == null || nodeIds == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 删除原本的权限列表
            roleMapper.deleteRoleNodes(roleId);
            // 3. 删除Redis数据库中存储的权限
            redisTemplate.delete(ROLE_PERMISSION_KEY + roleId.toString());
            // 4. 插入新的权限列表
            if ( roleMapper.insertRoleAuth(roleId, nodeIds) > 0 ) {
                logger.info("修改角色权限成功, roleId: {}, nodeIds: {}, 即将通知已登录用户刷新权限", roleId, nodeIds);
                Map<String, Object> message = Map.of("roleId", roleId, "nodeIds", nodeIds);
                // 5. 后端修改权限成功，通知已登录用户刷新前端权限
                rabbitTemplate.convertAndSend("permission.exchange", "permission.change." + roleId, message);
                logger.info("已通知登录用户刷新权限，修改角色权限成功");
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("修改角色权限失败, roleId: {}, nodeIds: {}", roleId, nodeIds);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( DataAccessException e ) {
            // 6. 数据输入异常处理
            logger.warn("由于输入不符合数据库约束，修改角色权限失败: {}", e.getMessage(), e);
            throw new UpdateException("由于输入不符合数据库约束，修改角色权限失败", e);
        } catch ( Exception e ) {
            // 7. 其他异常处理
            logger.error("修改角色权限时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("修改角色权限时发生异常", e);
        }
    }

    /**
     * 根据角色ID获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    private List<SystemNode> getNodesByRoleId(Integer roleId) {
        // 1. 从 Redis 缓存中获取角色权限列表
        List<SystemNode> nodeList;
        String redisKey = ROLE_PERMISSION_KEY + roleId.toString();
        try {
            nodeList = redisTemplate.opsForList().range(redisKey, 0, -1);
            // 2. 如果缓存中没有数据，则从数据库中查询
            if ( nodeList == null || nodeList.isEmpty() ) {
                logger.info("Redis 缓存中没有角色权限列表，从数据库中查询");
                nodeList = roleMapper.selectNodesByRoleId(roleId);
                redisTemplate.opsForList().rightPushAll(redisKey, nodeList);
                redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
            }
        } catch ( RedisCommandTimeoutException e ) {
            // 3. Redis 查询超时，从数据库中查询
            logger.warn("Redis 查询超时，从数据库中查询角色权限列表");
            nodeList = roleMapper.selectNodesByRoleId(roleId);
        }
        return nodeList;
    }
}
