package com.neusoft.neu24.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;

public interface ISystemNodeService extends IService<SystemNode> {

    /**
     * 查询所有系统功能节点树
     * @return 系统功能节点树
     */
    HttpResponseEntity<SystemNode> selectAllByTree();

}
