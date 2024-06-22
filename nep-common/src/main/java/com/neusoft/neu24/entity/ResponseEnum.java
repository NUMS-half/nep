package com.neusoft.neu24.entity;

import lombok.Getter;

/**
 * 系统API状态码与信息描述枚举类
 *
 * @author wyx
 * @since 2024-05-21
 */
@Getter
public enum ResponseEnum {

    /**
     * code规则:
     * 1. 2xx: 服务端正常返回
     * 2. 3xx: 用户行为问题导致失败
     * 3. 4xx: 服务端资源问题导致失败
     * 4. 5xx: 服务端运行时发生异常
     */

    /* 枚举项 */
    SUCCESS(200, "请求成功"),
    LOGIN_SUCCESS(201, "用户登录成功"),
    REGISTER_SUCCESS(202, "用户注册成功"),
    UPDATE_SUCCESS(203, "用户信息更新成功"),
    RESULT_IS_NULL(204, "查询结束，结果为空"),

    // 1.通用
    ADD_FAIL(300, "数据添加失败，请检查输入"),
    UPDATE_FAIL(300, "数据修改失败，请检查输入"),
    DELETE_FAIL(300, "数据删除失败，请检查输入"),
    USER_NOT_EXIST(300, "当前用户不存在"),
    CONTENT_IS_NULL(300, "内容不能为空"),
    PHONE_INVALID(300, "手机号码不合法"),
    REGION_INVALID(300, "网格信息不合法"),
    STATE_INVALID(300, "状态码不合法"),
    AQI_LEVEL_INVALID(300, "AQI等级不合法"),


    // 2.登录
    LOGIN_CONTENT_IS_NULL(301, "【账号】或【密码】不能为空"),
    LOGIN_FAIL(301, "【账号】或【密码】错误"),
    SMS_CODE_ERROR(301, "登录验证码【错误】或【过期】"),

    // 3.注册
    REGISTER_FAIL(302, "注册失败：用户名/手机号【已被注册】或【输入不合法】"),

    // 4.反馈上报
    REPORT_FAIL_ESTIMATE_INVALID(303, "预估AQI等级不合法"),

    // 5.反馈指派
    ASSIGN_FAIL_HAS_ASSIGNED(312, "该反馈信息【已被指派】或【已完成实测】"),
    ASSIGN_FAIL_UPDATE_FAIL(313, "反馈信息更新失败，指派失败"),
    ASSIGN_FAIL_NO_GM(302, "无匹配的网格员"),


    BAD_REQUEST(400, "请求失败"),
    UNAUTHORIZED(401, "未授权的请求，请登录后重试"),
    FORBIDDEN(403, "抱歉，您无权访问本系统"),
    NOT_FOUND(404, "访问的资源不存在"),

    SERVER_ERROR(500, "服务器接口异常"),
    SERVICE_UNAVAILABLE(503, "服务不可用");


    // 状态码
    private final Integer code;

    // 提示消息
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
