package com.mueeee.unpackerpanel.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 异步请求返回结果封装
 */
public class ApiResult {

    private int code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object data;

    public static ApiResult success() {
        return new ApiResult(Constant.OPERATE_SUCCESS, Constant.OPERATE_SUCCESS_STR, null);
    }

    public static ApiResult success(String message) {
        return new ApiResult(Constant.OPERATE_SUCCESS, message, null);
    }

    public static ApiResult success(Object data) {
        return new ApiResult(Constant.OPERATE_SUCCESS, Constant.OPERATE_SUCCESS_STR, data);
    }

    public static ApiResult success(String message, Object data) {
        return new ApiResult(Constant.OPERATE_SUCCESS, message, data);
    }
    public static ApiResult success(int code, String message, Object data) {
        return new ApiResult(code, message, data);
    }

    // ...

    public static ApiResult failure() {
        return new ApiResult(Constant.OPERATE_ERROR, Constant.OPERATE_ERROR_STR, null);
    }

    public static ApiResult failure(String message) {
        return new ApiResult(Constant.OPERATE_ERROR, message, null);
    }

    public static ApiResult failure(Object data) {
        return new ApiResult(Constant.OPERATE_ERROR, Constant.OPERATE_ERROR_STR, data);
    }

    public static ApiResult failure(String message, Object data) {
        return new ApiResult(Constant.OPERATE_ERROR, message, data);
    }

    // ...

    public static ApiResult failure(int code, String message) {
        return new ApiResult(code, message, null);
    }

    public static ApiResult failure(int code, String message, Object data) {
        return new ApiResult(code, message, data);
    }

    public ApiResult() {
    }

    private ApiResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.code == Constant.OPERATE_SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
