package com.light.web.common.exception;

import com.light.core.exception.CodeMessage;

public enum EnumApiException implements CodeMessage {
    UNKNOWN("40001", "非法请求"),

    TIMEOUT("40002", "服务超时"),

    THRESHOLD("40003", "接口调用频繁"),

    INVALID("40004", "无效的通讯数据"),

    VALIDATE("40005", "参数异常"),

    IPLEGAL("40006", "IP不合法"),

    APP_AUTH("40007", "无效的appId"),

    ACCESS_TOKEN_EXPIRED("40008", "授权已过期"),;

    private String code;

    private String message;

    private EnumApiException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String code() {
        return this.getCode();
    }

    @Override
    public String message() {
        return this.getMessage();
    }

    public static EnumApiException getByCode(String code) {
        for (EnumApiException value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
