package com.neusoft.neu24.client.fallback;

import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.exceptions.UpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    /**
     * Returns an instance of the fallback appropriate for the given cause.
     *
     * @param cause cause of an exception.
     * @return fallback
     */
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public HttpResponseEntity<User> selectUser(Map<String, Object> userInfo) {
                log.error("[UserService]查询用户信息失败: {}", cause.getMessage());
                return null; // 也可抛出异常，如RuntimeException
            }

            @Override
            public HttpResponseEntity<List<User>> selectAllUser() {
                log.error("[UserService]查询所有用户信息信息失败: {}", cause.getMessage());
                return null;
            }

            @Override
            public HttpResponseEntity<List<User>> selectBatchUser(List<String> userIds) {
                log.error("[UserService]批量查询用户信息失败: {}", cause.getMessage());
                return null;
            }

            @Override
            public HttpResponseEntity<List<User>> selectGridManagers(Map<String, Object> gmInfo) {
                log.error("[UserService]查询网格管理员信息失败: {}", cause.getMessage());
                return new HttpResponseEntity<List<User>>().success(new ArrayList<>());
            }

            @Override
            public HttpResponseEntity<Boolean> updateGmState(String gmUserId, Integer state) {
                throw new UpdateException("[UserService]修改网格员工作状态失败");
            }
        };
    }
}
