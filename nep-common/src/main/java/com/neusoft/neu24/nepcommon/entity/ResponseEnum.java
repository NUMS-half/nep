package com.neusoft.neu24.nepcommon.entity;

import lombok.Getter;

@Getter
public enum ResponseEnum {

    LOGIN_SUCCESS(200, "Login success."),
    LOGIN_FAIL(400, "Login failed."),
    NO_LOGIN(401, "Login required."),
    REGISTER_SUCCESS(201, "Register success."),
    REGISTER_FAIL(402, "Register failed."),
    UPDATE_SUCCESS(202, "Updated."),
    UPDATE_FAIL(403, "Update failed."),
    GET_SUCCESS(200, "Success."),
    GET_FAIL(404, "Not found."),
    ERROR(500, "Server error.");

    private Integer code;
    private String msg;

    ResponseEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private void setCode(Integer code) {
        this.code = code;
    }

    private void setMsg(String msg) {
        this.msg = msg;
    }

}
