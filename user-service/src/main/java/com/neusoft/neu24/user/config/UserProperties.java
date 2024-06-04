package com.neusoft.neu24.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nep.user")
public class UserProperties {

    // 测试热配置管理，以最大登录错误次数为例
    private Integer loginMaxTimes;
}
