package com.neusoft.neu24.entity;

import com.neusoft.neu24.dto.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponseEntity<T> {

    private Integer code;  //状态码

    private String msg;    //状态消息

    private T data;        //返回数据

    /**
     * 资源请求成功
     *
     * @param data 请求到的数据
     */
    public HttpResponseEntity<T> success(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SUCCESS, data);
    }

    /**
     * 用户登录成功
     *
     * @param data 登录成功的用户信息
     */
    public HttpResponseEntity<T> loginSuccess(T data) {
        return new HttpResponseEntity<>(ResponseEnum.LOGIN_SUCCESS, data);
    }

    public HttpResponseEntity<T> serverError(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SERVER_ERROR, data);
    }

    public HttpResponseEntity<T> resultIsNull(T data) {
        return new HttpResponseEntity<>(ResponseEnum.RESULT_IS_NULL, data);
    }

    public HttpResponseEntity<T> addFail(T data) {
        return new HttpResponseEntity<>(ResponseEnum.ADD_FAIL, data);
    }

    public HttpResponseEntity<T> serviceUnavailable(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SERVICE_UNAVAILABLE, data);
    }

    public HttpResponseEntity<Boolean> assignFail(Boolean data,ResponseEnum responseEnum) {
        return new HttpResponseEntity<>(responseEnum, data);
    }

    // 用户登录注册
    public static final HttpResponseEntity<UserDTO> LOGIN_FAIL = new HttpResponseEntity<>(ResponseEnum.LOGIN_FAIL, null);
    public static final HttpResponseEntity<UserDTO> LOGIN_CONTENT_IS_NULL = new HttpResponseEntity<>(ResponseEnum.LOGIN_CONTENT_IS_NULL, null);
    public static final HttpResponseEntity<UserDTO> REGISTER_FAIL = new HttpResponseEntity<>(ResponseEnum.REGISTER_FAIL, null);

    // 修改
    public static final HttpResponseEntity<Boolean> UPDATE_FAIL = new HttpResponseEntity<>(ResponseEnum.UPDATE_FAIL, null);
    public static final HttpResponseEntity<Boolean> STATE_INVALID = new HttpResponseEntity<>(ResponseEnum.STATE_INVALID, null);

    // 删除
    public static final HttpResponseEntity<Boolean> DELETE_FAIL = new HttpResponseEntity<>(ResponseEnum.DELETE_FAIL, null);

    // 消息
    public static final HttpResponseEntity<String> CONTENT_NULL = new HttpResponseEntity<>(ResponseEnum.CONTENT_NULL, null);

    /**
     * 请求失败
     *
     * @return 请求失败的响应实体
     */
    public HttpResponseEntity<T> failure() {
        return new HttpResponseEntity<>(ResponseEnum.BAD_REQUEST, null);
    }

    /**
     * 无权限访问
     *
     * @return 无权限访问的响应实体
     */
    public HttpResponseEntity<String> unauthorized(String msg, String detail) {
        return new HttpResponseEntity<>(401, msg, detail);
    }

    public HttpResponseEntity(ResponseEnum response, T data) {
        this.code = response.getCode();
        this.msg = response.getMsg();
        this.data = data;
    }

    public HttpResponseEntity(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
