package com.neusoft.neu24.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限校验路径配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "nep.permission")
public class PermissionProperties {
    private List<String> includePaths;
    private List<String> excludePaths;
    private String roleServiceBaseUrl;
}
