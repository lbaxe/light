package com.light.mapper.exception;

import java.sql.SQLException;

import com.light.core.exception.CodeMessage;

public class MapperException extends RuntimeException implements CodeMessage {
    private static final String DEFAULT_CODE = "-1";

    private SQLException sqlException;

    public SQLException getSqlException() {
        return sqlException;
    }

    public MapperException() {
        super();
    }

    public MapperException(String message) {
        super(message);
    }

    public MapperException(String message, Throwable t) {
        super(message, t);
        if (t instanceof SQLException) {
            this.sqlException = (SQLException)t;
        }
    }

    @Override
    public String code() {
        return DEFAULT_CODE;
    }

    @Override
    public String message() {
        return getMessage();
    }

}
