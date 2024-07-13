package com.neusoft.neu24.role.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.role.service.IRoleService;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <b>角色服务前端控制器</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    /**
     * 角色业务层接口
     */
    @Resource
    private IRoleService roleService;

    /**
     * 添加角色
     *
     * @param map 角色信息
     * @return 角色信息
     */
    @PostMapping(value = "/add", headers = "Accept=application/json")
    public HttpResponseEntity<Role> addRole(@RequestBody Map<String, Object> map) {
        try {
            // 1. 封装待添加的角色信息
            Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
            // 2. 添加角色
            return roleService.addRole(role);
        } catch ( SaveException e ) {
            logger.error("添加角色发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Role>().serverError(null);
        }
    }

    /**
     * 查询所有角色信息
     *
     * @return 所有角色信息
     */
    @PostMapping(value = "/select/all")
    public HttpResponseEntity<List<Role>> selectAll() {
        try {
            return roleService.selectAll();
        } catch ( QueryException e ) {
            logger.error("查询所有角色信息发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<Role>>().serverError(null);
        }
    }

    /**
     * 条件分页查询角色信息
     *
     * @param map     查询条件
     * @param current 当前页
     * @param size    页大小
     * @return 角色信息分页
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<Role>> selectRoleByPage(@RequestBody Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {
        try {
            // 1. 判断查询条件是否为空，若为空则查询所有角色信息
            if ( map == null || map.isEmpty() ) {
                return roleService.selectRoleByPage(null, current, size);
            }
            // 2. 不为空条件查询
            else {
                // 3. 封装查询条件
                Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
                return roleService.selectRoleByPage(role, current, size);
            }
        } catch ( QueryException e ) {
            logger.error("条件分页查询角色信息发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<IPage<Role>>().serverError(null);
        }
    }

    /**
     * 查询角色权限ID列表
     *
     * @param roleId 角色ID
     * @return 角色权限ID列表
     */
    @GetMapping(value = "/select/node")
    public HttpResponseEntity<List<Integer>> selectSystemNodeById(@RequestParam("roleId") Integer roleId) {
        try {
            return roleService.selectNodeIdsByRoleId(roleId);
        } catch ( QueryException e ) {
            logger.error("查询角色权限ID列表发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<Integer>>().serverError(null);
        }
    }

    /**
     * 查询角色权限列表
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    @GetMapping(value = "/select")
    public HttpResponseEntity<List<SystemNode>> selectNodesByRoleId(@RequestParam("roleId") Integer roleId) {
        try {
            return roleService.selectNodesByRoleId(roleId);
        } catch ( QueryException e ) {
            logger.error("查询角色权限列表发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<SystemNode>>().serverError(null);
        }
    }

    /**
     * 修改角色状态
     *
     * @param map 角色ID和状态
     * @return 是否修改成功
     */
    @PutMapping(value = "/update/state", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateState(@RequestBody Map<String, Object> map) {
        try {
            // 1. 获取角色ID和状态
            Integer roleId = (Integer) map.get("roleId");
            Integer state = (Integer) map.get("state");
            // 2. 修改角色状态
            return roleService.changeState(roleId, state);
        } catch ( Exception e ) {
            logger.error("修改角色状态发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    @PutMapping(value = "/delete", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> deleteRole(@Param("roleId") Integer roleId) {
        try {
            return roleService.deleteRole(roleId);
        } catch ( UpdateException e ) {
            logger.error("删除角色发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 修改角色信息
     *
     * @param map 角色信息
     * @return 是否修改成功
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateRole(@RequestBody Map<String, Object> map) {
        try {
            Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
            return roleService.updateRole(role);
        } catch ( Exception e ) {
            logger.error("修改角色信息发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 修改角色权限信息
     *
     * @param map 角色ID和权限ID列表
     * @return 是否修改成功
     */
    @PutMapping(value = "/update/auth", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateRoleAuth(@RequestBody Map<String, Object> map) {
        try {
            Integer roleId = (Integer) map.get("roleId");
            List<Integer> nodeIds = (List<Integer>) map.get("nodeIds");
            return roleService.updateRoleAuth(roleId, nodeIds);
        } catch ( Exception e ) {
            logger.error("修改角色权限信息发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

}
