package com.neusoft.neu24.role.controller;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.service.ISystemNodeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <b>系统节点前端控制器</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@RestController
@RequestMapping("/node")
public class SystemNodeController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(SystemNodeController.class);

    /**
     * 系统节点业务层接口
     */
    @Resource
    private ISystemNodeService systemNodeService;

    /**
     * 查询指定节点的子树
     *
     * @param nodeId 指定节点ID
     * @return 指定节点功能子树
     */
    @GetMapping("/select/tree")
    public HttpResponseEntity<List<SystemNode>> selectTree(@RequestParam("nodeId") Integer nodeId) {
        try {
            return systemNodeService.getSubtree(nodeId);
        } catch ( Exception e ) {
            logger.error("查询指定节点的子树发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<SystemNode>>().serverError(null);
        }
    }
}
