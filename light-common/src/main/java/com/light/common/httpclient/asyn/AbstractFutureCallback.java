package com.light.common.httpclient.asyn;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFutureCallback implements FutureCallback<HttpResponse> {
    Logger logger = LoggerFactory.getLogger(AbstractFutureCallback.class);

    @Override
    public void completed(HttpResponse httpResponse) {
        doCompleted(httpResponse);
    }

    @Override
    public void failed(Exception e) {
        logger.error("http asyn failed", e);
    }

    @Override
    public void cancelled() {
        logger.warn("http asyn cancelled");
    }

    protected abstract void doCompleted(HttpResponse httpResponse);
}
