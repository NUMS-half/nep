package com.neusoft.neu24.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.neu24.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 更新角色状态
     * @param roleId 角色ID
     * @param state 角色状态
     * @return 更新是否成功
     */
    @Update("UPDATE role SET state = #{state} WHERE role_id = #{roleId}")
    int updateState(@Param("roleId") Integer roleId, @Param("state") Integer state);

    /**
     * 查询启用的角色的权限(子节点)列表
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    List<Integer> selectSystemNodeById(@Param("roleId") Integer roleId);

    int insertRoleAuth(@Param("roleId") Integer roleId, @Param("nodeIds") List<Integer> nodeIds);
}
