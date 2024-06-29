package com.neusoft.neu24.role.controller;

import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.SystemNode;
import com.neusoft.neu24.role.service.ISystemNodeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/node")
public class SystemNodeController {

    @Resource
    private ISystemNodeService systemNodeService;

    @GetMapping("/select/tree")
    public HttpResponseEntity<List<SystemNode>> selectTree(@RequestParam("nodeId") Integer nodeId) {
        return systemNodeService.getSubtree(nodeId);
    }
}
