package com.neusoft.neu24.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.mapper.SystemNodeMapper;
import com.neusoft.neu24.role.service.ISystemNodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemNodeServiceImpl extends ServiceImpl<SystemNodeMapper, SystemNode> implements ISystemNodeService {

    @Resource
    private SystemNodeMapper systemNodeMapper;

    /**
     * 查询指定节点的子树
     *
     * @param nodeId 指定节点ID
     * @return 指定节点子树
     */
    @Override
    public HttpResponseEntity<List<SystemNode>> getSubtree(Integer nodeId) {
        List<SystemNode> nodes = systemNodeMapper.selectSubtree(nodeId);
        List<SystemNode> forest = buildTree(nodes, nodeId);
        List<SystemNode> tree = new ArrayList<>();
        SystemNode root = nodes.get(0);
        root.setChildren(forest);
        tree.add(root);
        return new HttpResponseEntity<List<SystemNode>>().success(tree);
    }

    /**
     * 构建树形结构
     *
     * @param nodes    系统节点列表
     * @param parentId 父节点ID
     * @return 树
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
