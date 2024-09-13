package com.light.common.httpclient.asyn;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFutureCallback extends AbstractFutureCallback {
    Logger logger = LoggerFactory.getLogger(DefaultFutureCallback.class);

    @Override
    protected void doCompleted(HttpResponse httpResponse) {
        // do nothing
    }
}
