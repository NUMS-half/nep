package com.neusoft.neu24.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.mapper.SystemNodeMapper;
import com.neusoft.neu24.role.service.ISystemNodeService;
import org.springframework.stereotype.Service;

@Service
public class SystemNodeServiceImpl extends ServiceImpl<SystemNodeMapper, SystemNode> implements ISystemNodeService {


    /**
     * 查询所有系统功能节点树
     *
     * @return 系统功能节点树
     */
    @Override
    public HttpResponseEntity<SystemNode> selectAllByTree() {
        return null;
    }
}
