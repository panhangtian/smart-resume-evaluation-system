package com.example.resume.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
@Schema(description = "统一响应")
public class R<T> implements Serializable {

    @Schema(description = "状态码")
    private int code;

    @Schema(description = "消息")
    private String msg;

    @Schema(description = "数据")
    private T data;

    public static <T> R<T> ok() {
        return result(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return result(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return result(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> failed(String msg) {
        return result(ResultCode.FAILED.getCode(), msg, null);
    }

    public static <T> R<T> failed(int code, String msg) {
        return result(code, msg, null);
    }

    public static <T> R<T> result(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
