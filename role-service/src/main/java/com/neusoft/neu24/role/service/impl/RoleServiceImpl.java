package com.neusoft.neu24.role.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.mapper.RoleMapper;
import com.neusoft.neu24.role.service.IRoleService;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.annotation.Resource;
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

@Service
@Transactional
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Resource
    private RoleMapper roleMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

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
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", -1);
        List<Role> list = roleMapper.selectList(queryWrapper);
        if ( list == null || list.isEmpty() ) {
            return new HttpResponseEntity<List<Role>>().resultIsNull(null);
        } else {
            return new HttpResponseEntity<List<Role>>().success(list);
        }
    }

    /**
     * 条件分页查询角色信息
     *
     * @return 分页的角色信息
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<Role>> selectRoleByPage(Role role, long current, long size) {
        IPage<Role> page = new Page<>(current, size);
        IPage<Role> pages;
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Role::getState, -1);
        if ( role != null ) {
            if ( role.getState() != null ) {
                queryWrapper.eq(Role::getState, role.getState());
            }
            queryWrapper.and(wrapper -> {
                wrapper.like(Role::getRoleName, role.getRoleName());
                wrapper.or().like(Role::getRemark, role.getRemark());
            });
        }
        pages = getBaseMapper().selectPage(page, queryWrapper);
        return pages == null || pages.getTotal() == 0 ?
                new HttpResponseEntity<IPage<Role>>().resultIsNull(null) :
                new HttpResponseEntity<IPage<Role>>().success(pages);
    }

    /**
     * 根据角色id查询角色信息
     *
     * @param role 角色信息
     * @return 角色信息
     */
    @Override
    public HttpResponseEntity<Role> addRole(Role role) {
        // 保证可以正常插入自增ID
        role.setRoleId(null);
        // 默认状态为启用
        role.setState(1);
        try {
            return roleMapper.insert(role) > 0 ?
                    new HttpResponseEntity<Role>().success(role) :
                    new HttpResponseEntity<Role>().fail(ResponseEnum.ADD_FAIL);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Role>().fail(ResponseEnum.ADD_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Role>().serverError(null);
        }
    }

    /**
     * 添加角色
     *
     * @param role 角色信息
     * @return 是否修改成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateRole(Role role) {
        if ( role.getRoleId() == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        }
        try {
            return roleMapper.updateById(role) > 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
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
        try {
            return roleMapper.updateState(roleId, state) != 0 ?
                    new HttpResponseEntity<Boolean>().success(null) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
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
        if ( roleId == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.DELETE_FAIL);
        }
        try {
            return roleMapper.updateState(roleId, -1) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
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
        List<Integer> list = roleMapper.selectNodeIdsByRoleId(roleId);
        if ( list == null || list.isEmpty() ) {
            return new HttpResponseEntity<List<Integer>>().resultIsNull(null);
        }
        return new HttpResponseEntity<List<Integer>>().success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<SystemNode>> selectNodesByRoleId(Integer roleId) {
        try {
            List<SystemNode> nodeList = getNodesByRoleId(roleId);
            return CollUtil.isEmpty(nodeList) ?
                    new HttpResponseEntity<List<SystemNode>>().resultIsNull(null) :
                    new HttpResponseEntity<List<SystemNode>>().success(nodeList);
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
        if ( roleId == null || nodeIds == null ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        }
        try {
            roleMapper.deleteRoleNodes(roleId);
            redisTemplate.delete(ROLE_PERMISSION_KEY + roleId.toString());
            if (roleMapper.insertRoleAuth(roleId, nodeIds) > 0) {
                logger.info("修改角色权限成功, roleId: {}, nodeIds: {}, 即将通知已登录用户刷新权限", roleId, nodeIds);
                Map<String, Object> message = Map.of("roleId", roleId, "nodeIds", nodeIds);
                rabbitTemplate.convertAndSend("permission.exchange", "permission.change." + roleId, message);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("修改角色权限失败, roleId: {}, nodeIds: {}", roleId, nodeIds);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    private List<SystemNode> getNodesByRoleId(Integer roleId) {
        List<SystemNode> nodeList;
        String redisKey = ROLE_PERMISSION_KEY + roleId.toString();
        try {
            nodeList = redisTemplate.opsForList().range(redisKey, 0, -1);
            if ( nodeList == null || nodeList.isEmpty() ) {
                nodeList = roleMapper.selectNodesByRoleId(roleId);
                redisTemplate.opsForList().rightPushAll(redisKey, nodeList);
                redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
            }
        } catch ( RedisCommandTimeoutException e ) {
            nodeList = roleMapper.selectNodesByRoleId(roleId);
        }
        return nodeList;
    }
}
