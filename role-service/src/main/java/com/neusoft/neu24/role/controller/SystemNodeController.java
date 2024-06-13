package com.neusoft.neu24.role.controller;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.service.ISystemNodeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/node")
public class SystemNodeController {

    @Resource
    private ISystemNodeService systemNodeService;

    @GetMapping("/select/tree/{nodeId}")
    public HttpResponseEntity<List<SystemNode>> selectTree(@PathVariable("nodeId") Integer nodeId) {
        return systemNodeService.getSubtree(nodeId);
    }
}
