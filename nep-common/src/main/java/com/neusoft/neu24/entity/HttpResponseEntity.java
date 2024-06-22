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
     * 枚举构造器
     */
    public HttpResponseEntity(ResponseEnum response, T data) {
        this.code = response.getCode();
        this.msg = response.getMsg();
        this.data = data;
    }

    /**
     * 全参构造器
     */
    public HttpResponseEntity(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    /**
     * 资源请求成功(2xx)
     */
    public HttpResponseEntity<T> success(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SUCCESS, data);
    }

    public HttpResponseEntity<T> success(ResponseEnum responseEnum, T data) {
        return new HttpResponseEntity<>(responseEnum, data);
    }

    /**
     * 请求失败(3xx)
     */
    public HttpResponseEntity<T> fail(ResponseEnum responseEnum) {
        return new HttpResponseEntity<>(responseEnum, null);
    }

    public HttpResponseEntity<T> fail(ResponseEnum responseEnum, T data) {
        return new HttpResponseEntity<>(responseEnum, data);
    }

    /**
     * 结果为空(204)
     */
    public HttpResponseEntity<T> resultIsNull(T data) {
        return new HttpResponseEntity<>(ResponseEnum.RESULT_IS_NULL, data);
    }

    /**
     * 请求错误(4xx)
     */
    public HttpResponseEntity<T> error(ResponseEnum responseEnum) {
        return new HttpResponseEntity<>(responseEnum, null);
    }

    /**
     * 未授权(401)
     */
    public HttpResponseEntity<String> unauthorized(String msg, String detail) {
        return new HttpResponseEntity<>(401, msg, detail);
    }

    /**
     * 服务器异常(5xx)
     */
    public HttpResponseEntity<T> serverError(T data) {
        return new HttpResponseEntity<>(ResponseEnum.SERVER_ERROR, data);
    }

}
