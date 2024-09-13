package com.light.common.httpclient;

public interface CallBack<T> {
    T call(String data);
}