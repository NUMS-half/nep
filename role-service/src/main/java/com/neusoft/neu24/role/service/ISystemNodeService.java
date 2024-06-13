package com.neusoft.neu24.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;

import java.util.List;

public interface ISystemNodeService extends IService<SystemNode> {

    /**
     * 查询指定节点的子树
     *
     * @param nodeId 指定节点ID
     * @return 指定节点子树
     */
    HttpResponseEntity<List<SystemNode>> getSubtree(Integer nodeId);

}
