package com.neusoft.neu24.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.neu24.entity.Role;
import com.neusoft.neu24.entity.SystemNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;


/**
 * <b>Role类 Mapper 接口</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
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
     * 查询启用的角色的权限ID(子节点)列表
     * @param roleId 角色ID
     *
     * @return 角色权限ID列表
     */
    List<Integer> selectNodeIdsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询启用的角色的权限列表
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    List<SystemNode> selectNodesByRoleId(@Param("roleId") Integer roleId);

    /**
     * 逻辑删除角色
     *
     * @param roleId 角色ID
     * @return 影响的行数
     */
    int deleteRoleNodes(@Param("roleId") Integer roleId);

    /**
     * 为角色添加权限
     * @param roleId  角色ID
     * @param nodeIds 权限ID列表
     * @return 影响的行数
     */
    int insertRoleAuth(@Param("roleId") Integer roleId, @Param("nodeIds") List<Integer> nodeIds);
}
