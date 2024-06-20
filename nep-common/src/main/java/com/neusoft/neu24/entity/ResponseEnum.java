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

    LOGIN_CONTENT_IS_NULL(300, "用户名或密码不能为空"),
    REGISTER_FAIL(301, "注册失败：用户名/手机号已被注册或输入不合法"),
    LOGIN_FAIL(303, "用户名或密码错误"),
    UPDATE_FAIL(302, "信息修改失败，请检查输入"),
    ADD_FAIL(304, "信息新增失败，请检查输入"),
    ASSIGN_FAIL_NO_GM(305, "无匹配的网格员"),
    PHONE_INVALID(306, "手机号码不合法"),
    SMS_CODE_ERROR(307, "验证码错误或验证码过期"),
    STATE_INVALID(308, "状态码不合法"),
    CONTENT_NULL(309, "内容不能为空"),
    USER_NOT_EXIST(310, "用户已销户，请重新注册"),
    DELETE_FAIL(311, "删除失败"),
    ASSIGN_FAIL_HAS_ASSIGNED(312, "该反馈上报信息已被指派或已完成"),
    ASSIGN_FAIL_UPDATE_FAIL(313, "反馈信息更新失败"),

    BAD_REQUEST(400, "请求失败"),
    UNAUTHORIZED(401, "未授权的请求，请登录后重试"),
    FORBIDDEN(403, "抱歉，您无权访问本系统"),
    NOT_FOUND(404, "访问的资源不存在"),

    SERVICE_UNAVAILABLE(503, "服务不可用"),

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
