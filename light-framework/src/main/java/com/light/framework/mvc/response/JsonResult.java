package com.light.framework.mvc.response;

import java.util.StringJoiner;

/**
 * 操作消息提醒
 * 
 * @author ruoyi
 */
public class JsonResult<T> {
    public static final String DEFAULT_SUCCESS_CODE = "200";// ref http状态200 OK
    public static final String DEFAULT_ERROR_CODE = "500";// ref http状态500 Internal Server Error
    /** 状态码 */
    private String code;

    /** 返回内容 */
    private String msg;

    /** 数据对象 */
    private T data;

    /**
     * 使其表示一个空消息。
     */
    public JsonResult() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回成功消息
     * 
     * @return 成功消息
     */
    public static JsonResult success() {
        return JsonResult.success("操作成功");
    }

    /**
     * 返回成功数据
     * 
     * @return 成功消息
     */
    public static <T> JsonResult success(T data) {
        return JsonResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     * 
     * @param msg 返回内容
     * @return 成功消息
     */
    public static JsonResult success(String msg) {
        return JsonResult.success(msg, null);
    }

    /**
     * 返回成功消息
     * 
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> JsonResult success(String msg, T data) {
        JsonResult result = new JsonResult();
        result.setCode(DEFAULT_SUCCESS_CODE);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 返回错误消息
     * 
     * @return
     */
    public static JsonResult error() {
        return JsonResult.error("操作失败");
    }

    /**
     * 返回错误消息
     * 
     * @param msg 返回内容
     * @return 警告消息
     */
    public static JsonResult error(String msg) {
        return JsonResult.error(DEFAULT_ERROR_CODE, msg);
    }

    /**
     * 返回错误消息
     * 
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> JsonResult error(String code, String msg) {
        return JsonResult.error(code, msg, null);
    }

    /**
     * 返回错误消息
     * 
     * @param code 状态码
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> JsonResult error(String code, String msg, T data) {
        JsonResult result = new JsonResult();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JsonResult.class.getSimpleName() + "[", "]").add("code='" + code + "'")
            .add("msg='" + msg + "'").add("data=" + data).toString();
    }
}
