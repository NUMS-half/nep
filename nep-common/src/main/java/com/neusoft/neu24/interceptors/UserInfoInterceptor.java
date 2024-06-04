package com.neusoft.neu24.interceptors;

import cn.hutool.core.bean.BeanUtil;
import com.neusoft.neu24.config.RedisConstants;
import com.neusoft.neu24.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取登录用户信息
        String userInfo = request.getHeader("userId");
        // 2. 判断是否获取了用户，获取到时将用户信息存入ThreadLocal
        if ( userInfo != null && !userInfo.isEmpty() ) {
            // 将用户信息存入ThreadLocal
            UserContext.setUser(userInfo);
        }
        // 3. 放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息
        UserContext.removeUser();
    }
}
