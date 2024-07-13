package com.neusoft.neu24.role.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.ResponseEnum;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.role.mapper.SystemNodeMapper;
import com.neusoft.neu24.role.service.ISystemNodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>系统节点业务层实现类</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
@Slf4j
@Service
@Transactional
public class SystemNodeServiceImpl extends ServiceImpl<SystemNodeMapper, SystemNode> implements ISystemNodeService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(SystemNodeServiceImpl.class);

    /**
     * 系统节点数据访问对象
     */
    @Resource
    private SystemNodeMapper systemNodeMapper;

    /**
     * 查询指定节点的子树
     *
     * @param nodeId 指定节点ID
     * @return 指定节点子树
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<SystemNode>> getSubtree(Integer nodeId) {
        // 1. 确保 nodeId 不为 null
        if ( nodeId == null ) {
            return new HttpResponseEntity<List<SystemNode>>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 查询指定节点的子树
            List<SystemNode> nodes = systemNodeMapper.selectSubtree(nodeId);
            if ( CollUtil.isEmpty(nodes) ) {
                logger.warn("功能节点: {} 的子功能树为空", nodeId);
                return new HttpResponseEntity<List<SystemNode>>().resultIsNull(null);
            }
            // 3. 查询到时，构建功能节点的树形结构
            List<SystemNode> forest = buildTree(nodes, nodeId);
            List<SystemNode> tree = new ArrayList<>();
            SystemNode root = nodes.get(0);
            root.setChildren(forest);
            tree.add(root);
            logger.info("功能节点: {} 的子功能树查询成功", nodeId);
            return new HttpResponseEntity<List<SystemNode>>().success(tree);
        } catch ( Exception e ) {
            logger.error("查询功能节点: {} 的子功能树发生异常: {}", nodeId, e.getMessage(), e);
            throw new QueryException("查询功能节点的子功能树发生异常", e);
        }
    }

    /**
     * 构建功能节点的树形结构
     *
     * @param nodes    系统节点列表
     * @param parentId 父节点ID
     * @return 功能节点树
     */
    private List<SystemNode> buildTree(List<SystemNode> nodes, Integer parentId) {
        List<SystemNode> result = new ArrayList<>();

        // 1. 确保 parentId 不为 null
        if ( parentId == null ) {
            return result;
        }

        // 2. 过滤出所有当前父节点的直接子节点
        List<SystemNode> children = nodes.stream()
                .filter(node -> parentId.equals(node.getPid()))
                .toList();

        // 3. 对每个子节点递归构建其子树
        for ( SystemNode child : children ) {
            List<SystemNode> childTree = buildTree(nodes, child.getNodeId());
            child.setChildren(childTree);
            result.add(child);
        }

        return result;
    }
}
