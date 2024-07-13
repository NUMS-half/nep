package com.neusoft.neu24.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;

import java.util.List;

/**
 * <b>系统节点业务层接口</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
public interface ISystemNodeService extends IService<SystemNode> {

    /**
     * 查询指定节点的子树
     *
     * @param nodeId 指定节点ID
     * @return 指定节点子树
     */
    HttpResponseEntity<List<SystemNode>> getSubtree(Integer nodeId);

}
