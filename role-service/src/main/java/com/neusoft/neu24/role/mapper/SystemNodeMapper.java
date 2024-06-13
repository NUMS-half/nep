package com.neusoft.neu24.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.neu24.entity.SystemNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemNodeMapper extends BaseMapper<SystemNode> {


    /**
     * 递归查询系统节点子树
     * @param nodeId 根节点ID
     * @return 子树列表
     */
    List<SystemNode> selectSubtree(@Param("nodeId") Integer nodeId);
}
