package com.light.framework.auth.token;

import java.io.Serializable;

public enum TokenStatus implements Serializable {
    SUCCESS(0),

    FAIL(1),

    DISABLED(2);

    private final int value;

    public int getValue() {
        return this.value;
    }

    TokenStatus(int value) {
        this.value = value;
    }

    public static TokenStatus getEnum(String value) {
        for (TokenStatus enumValue : values()) {
            if (enumValue.toString().equals(value))
                return enumValue;
        }
        return null;
    }

    public static TokenStatus getTokenStatus(int value) {
        for (TokenStatus enumValue : values()) {
            if (enumValue.value == value)
                return enumValue;
        }
        return null;
    }
}
