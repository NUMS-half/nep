package com.neusoft.neu24.gateway.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * jwt签名器
     */
    private final JWTSigner jwtSigner;

    public JwtUtil(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    /**
     * 创建 access-token
     *
     * @param userId 用户信息
     * @param ttl token有效时间
     * @return access-token
     */
    public String createToken(String userId, Duration ttl) {
        // 1.生成jws
        return JWT.create()
                .setPayload("user", userId)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析token
     *
     * @param token token
     * @return 解析刷新token得到的用户信息
     */
    public HttpResponseEntity<String> parseToken(String token) {
        // 1.校验token是否为空
        if (token == null) {
            return new HttpResponseEntity<>().unauthorized("未登录",null);
        }
        // 2.校验并解析jwt
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            return new HttpResponseEntity<>().unauthorized("无效的token", e.getMessage());
        }
        // 2.校验jwt是否有效
        if (!jwt.verify()) {
            // 验证失败
            return new HttpResponseEntity<>().unauthorized("无效的token", null);
        }
        // 3.校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch ( ValidateException e) {
            return new HttpResponseEntity<>().unauthorized("token已经过期", e.getMessage());
        }
        // 4.数据格式校验
        Object userPayload = jwt.getPayload("user");
        if (userPayload == null) {
            // 数据为空
            return new HttpResponseEntity<>().unauthorized("无效的token", null);
        }

        // 5.数据解析
        try {
            return new HttpResponseEntity<String>().success(String.valueOf(userPayload.toString()));
        } catch (RuntimeException e) {
            // 数据格式有误
            return new HttpResponseEntity<>().unauthorized("无效的token", null);
        }
    }
}
