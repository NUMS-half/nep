package com.neusoft.neu24.role.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.service.IRoleService;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role")
public class RoleController {

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
        Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
        return roleService.addRole(role);
    }

    /**
     * 查询所有角色信息
     */
    @PostMapping(value = "/select/all")
    public HttpResponseEntity<List<Role>> selectAll() {
        return roleService.selectAll();
    }

    /**
     * 分页查询角色信息
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<Role>> selectRoleByPage(@RequestBody Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {
        try {
            if ( map == null || map.isEmpty() ) {
                return roleService.selectRoleByPage(null, current, size);
            } else {
                Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
                return roleService.selectRoleByPage(role, current, size);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<IPage<Role>>().serverError(null);
        }
    }

    /**
     * 查询角色权限列表
     */
    @GetMapping(value = "/select/node")
    public HttpResponseEntity<List<Integer>> selectSystemNodeById(@RequestParam("roleId") Integer roleId) {
        return roleService.selectNodeIdsByRoleId(roleId);
    }

    @GetMapping(value = "/select")
    public HttpResponseEntity<List<SystemNode>> selectNodesByRoleId(@RequestParam("roleId") Integer roleId) {
        return roleService.selectNodesByRoleId(roleId);
    }

    /**
     * 修改角色状态
     */
    @PutMapping(value = "/update/state", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateState(@RequestBody Map<String, Object> map) {
        Integer roleId = (Integer) map.get("roleId");
        Integer state = (Integer) map.get("state");
        return roleService.changeState(roleId, state);
    }

    /**
     * 删除角色
     */
    @PutMapping(value = "/delete", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> deleteRole(@Param("roleId") Integer roleId) {
        return roleService.deleteRole(roleId);
    }

    /**
     * 修改角色信息
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateRole(@RequestBody Map<String, Object> map) {
        Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
        return roleService.updateRole(role);
    }

    /**
     * 修改角色权限信息
     */
    @PutMapping(value = "/update/auth", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateRoleAuth(@RequestBody Map<String, Object> map) {
        Integer roleId = (Integer) map.get("roleId");
        List<Integer> nodeIds = (List<Integer>) map.get("nodeIds");
        return roleService.updateRoleAuth(roleId, nodeIds);
    }

}
