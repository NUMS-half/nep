package com.neusoft.neu24.entity;

import lombok.Getter;

/**
 * ResponseEnum 系统API状态码与信息描述枚举
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Getter
public enum ResponseEnum {

    /**
     * code规则:
     * 1. 2xx: 服务端正常返回
     * 2. 3xx: 用户行为问题
     * 3. 4xx: 服务端数据问题
     * 4. 5xx: 服务端运行异常
     */

    /* 枚举项 */
    SUCCESS(200, "请求成功"),
    LOGIN_SUCCESS(201, "用户登录成功"),
    REGISTER_SUCCESS(202, "用户注册成功"),
    UPDATE_SUCCESS(203, "用户信息更新成功"),
    RESULT_IS_NULL(204, "查询结束，结果为空"),

    UNAUTHORIZED(300, "未授权的请求."),
    LOGIN_CONTENT_IS_NULL(301, "用户名或密码不能为空"),
    REGISTER_FAIL(302, "用户名已存在，或检查输入是否合法"),
    UPDATE_FAIL(303, "用户信息更新失败，请检查输入"),
    LOGIN_FAIL(304, "用户名或密码错误"),

    BAD_REQUEST(400, "请求失败"),
    FORBIDDEN(403, "资源不允许被访问"),
    NOT_FOUND(404, "访问的资源不存在"),

    SERVER_ERROR(500, "服务器接口异常");

    private final Integer code;
    private final String msg;

    /**
     * 无参默认构造器
     */
    ResponseEnum() {
        this.code = 200;
        this.msg = "Success.";
    }

    /**
     * 全参构造器
     * @param code 状态码
     * @param msg 状态消息
     */
    ResponseEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
