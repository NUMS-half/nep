package com.neusoft.neu24.role.controller;

import cn.hutool.core.bean.BeanUtil;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.role.service.IRoleService;
import jakarta.annotation.Resource;
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
     * @param map 角色信息
     * @return 角色信息
     */
    @PostMapping(value = "/add", headers = "Accept=application/json")
    public HttpResponseEntity<Role> addRole(@RequestBody Map<String,Object> map) {
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
     * 修改角色信息
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateRole(@RequestBody Map<String,Object> map) {
        Role role = BeanUtil.fillBeanWithMap(map, new Role(), false);
        return roleService.updateRole(role);
    }
}
