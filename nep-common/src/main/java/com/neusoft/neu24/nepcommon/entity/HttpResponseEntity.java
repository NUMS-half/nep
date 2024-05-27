package com.neusoft.neu24.nepcommon.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@NoArgsConstructor
public class HttpResponseEntity<T> {

    private Integer code;  //状态码

    private String msg;    //状态消息

    private T data;        //返回数据

    /**
     * 资源请求成功
     * @param data 请求到的数据
     */
    public HttpResponseEntity<T> success(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SUCCESS, data);
    }

    /**
     * 用户登录成功
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

    // 登录模块
    public static final HttpResponseEntity<String> LOGIN_FAIL = new HttpResponseEntity<>(ResponseEnum.LOGIN_FAIL, null);
    public static final HttpResponseEntity<String> LOGIN_CONTENT_IS_NULL = new HttpResponseEntity<>(ResponseEnum.LOGIN_CONTENT_IS_NULL, null);

    // 注册模块
    public static final HttpResponseEntity<Boolean> REGISTER_FAIL = new HttpResponseEntity<>(ResponseEnum.REGISTER_FAIL, null);

    // 修改模块
    public static final HttpResponseEntity<Boolean> UPDATE_FAIL = new HttpResponseEntity<>(ResponseEnum.UPDATE_FAIL, null);

    /**
     * 请求失败
     * @return 请求失败的响应实体
     */
    public HttpResponseEntity<T> failure() {
        return new HttpResponseEntity<>(ResponseEnum.BAD_REQUEST, null);
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

//    public ResponseEntity<HttpResponseEntity<T>> toResponseEntity() {
//        // 将整数状态码转换为 HttpStatus 枚举
//        HttpStatus httpStatus = HttpStatus.resolve(this.code);
//        if ( httpStatus == null ) {
//            // 如果 code 无法解析成有效的 HttpStatus，可以默认为 OK 或自定义错误
//            httpStatus = HttpStatus.OK;
//        }
//        // 返回构建的 ResponseEntity
//        return new ResponseEntity<>(this, httpStatus);
//    }
}
