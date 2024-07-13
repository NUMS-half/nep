package com.neusoft.neu24.role.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;

import java.util.List;

/**
 * <b>角色业务层接口</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
public interface IRoleService extends IService<Role> {

    /**
     * 查询所有角色信息
     *
     * @return 所有角色信息
     */
    HttpResponseEntity<List<Role>> selectAll();

    /**
     * 条件分页查询角色信息
     * @param role    查询条件
     * @param current 当前页
     * @param size    每页大小
     * @return        角色信息分页
     */
    HttpResponseEntity<IPage<Role>> selectRoleByPage(Role role, long current, long size);

    /**
     * 添加角色信息
     *
     * @param role 角色信息
     * @return 角色信息
     */
    HttpResponseEntity<Role> addRole(Role role);

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 是否修改成功
     */
    HttpResponseEntity<Boolean> updateRole(Role role);

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param state 状态
     * @return 是否修改成功
     */
    HttpResponseEntity<Boolean> changeState(Integer roleId, Integer state);

    /**
     * 删除角色(逻辑)
     *
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    HttpResponseEntity<Boolean> deleteRole(Integer roleId);

    /**
     * 查询角色权限ID列表
     *
     * @param roleId 角色ID
     * @return 角色权限ID列表
     */
    HttpResponseEntity<List<Integer>> selectNodeIdsByRoleId(Integer roleId);

    /**
     * 查询角色权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    HttpResponseEntity<List<SystemNode>> selectNodesByRoleId(Integer roleId);

    /**
     * 修改角色权限列表
     * @param roleId 角色ID
     * @param nodeIds 权限ID列表
     * @return 是否修改成功
     */
    HttpResponseEntity<Boolean> updateRoleAuth(Integer roleId, List<Integer> nodeIds);
}
