package com.neusoft.neu24.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录校验路径配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "nep.auth")
public class AuthProperties {
    private List<String> includePaths;
    private List<String> excludePaths;
}
