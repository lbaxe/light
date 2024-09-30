package com.light.core.exception;

public class ServiceException extends RuntimeException implements IMessageCode {

    private static final String DEFAULT_CODE = "500";

    /**
     * 错误码
     */
    private String code;

    public ServiceException(String message) {
        super(message);
        setCode(DEFAULT_CODE);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        setCode(DEFAULT_CODE);
    }

    public ServiceException(String code, String message) {
        super(message);
        setCode(code);
    }

    public ServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        setCode(code);
    }

    public ServiceException(IMessageCode messageCode) {
        super(messageCode.message());
        setCode(messageCode.code());
    }

    public ServiceException(IMessageCode messageCode, Throwable cause) {
        super(messageCode.message(), cause);
        setCode(messageCode.code());
    }

    private void setCode(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return getMessage();
    }
}