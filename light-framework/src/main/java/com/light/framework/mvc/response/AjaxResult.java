package com.light.framework.mvc.response;

/**
 * 操作消息提醒
 * 
 * @author ruoyi
 */
public class AjaxResult<T> {
    public static final String DEFAULT_SUCCESS_CODE = "200";// ref http状态200 OK
    public static final String DEFAULT_ERROR_CODE = "500";// ref http状态500 Internal Server Error
    /** 状态码 */
    private String code;

    /** 返回内容 */
    private String msg;

    /** 数据对象 */
    private T data;

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public AjaxResult() {}

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
    public static AjaxResult success() {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功数据
     * 
     * @return 成功消息
     */
    public static <T> AjaxResult success(T data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     * 
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     * 
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult success(String msg, T data) {
        AjaxResult result = new AjaxResult();
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
    public static AjaxResult error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回错误消息
     * 
     * @param msg 返回内容
     * @return 警告消息
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(DEFAULT_ERROR_CODE, msg);
    }

    /**
     * 返回错误消息
     * 
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> AjaxResult error(String code, String msg) {
        return AjaxResult.error(code, msg, null);
    }

    /**
     * 返回错误消息
     * 
     * @param code 状态码
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> AjaxResult error(String code, String msg, T data) {
        AjaxResult result = new AjaxResult();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    @Override
    public String toString() {
        return "AjaxResult [data=" + this.data + ", code=" + this.code + ", msg=" + this.msg + "]";
    }
}
