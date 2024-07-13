package com.neusoft.neu24.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * <b>JWT配置属性</b>’
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
@Data
@Component
@ConfigurationProperties(prefix = "nep.jwt")
public class JwtProperties {

    /**
     密钥文件路径
     *
     */
    private Resource location;

    /**
     * 密钥密码
     */
    private String password;

    /**
     * 密钥别名
     */
    private String alias;

    /**
     * Token有效期
     */
    private Duration tokenTTL = Duration.ofMinutes(10);
}
