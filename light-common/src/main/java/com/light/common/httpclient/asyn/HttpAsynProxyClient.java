package com.light.common.httpclient.asyn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.light.common.httpclient.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http请求的工具类
 */
public class HttpAsynProxyClient {
    Logger logger = LoggerFactory.getLogger(HttpAsynProxyClient.class);

    private CloseableHttpAsyncClient closeableHttpAsyncClient;

    public HttpAsynProxyClient(CloseableHttpAsyncClient closeableHttpAsyncClient) {
        this.closeableHttpAsyncClient = closeableHttpAsyncClient;
        this.closeableHttpAsyncClient.start();
    }

    private List<NameValuePair> buildPostData(Map<String, Object> params) {
        if (params == null || params.size() == 0) {
            return new ArrayList<>(0);
        }
        List<NameValuePair> ret = new ArrayList<>(params.size());
        for (String key : params.keySet()) {
            Object p = params.get(key);
            if (key != null && p != null) {
                BasicNameValuePair basicNameValuePair = new BasicNameValuePair(key, p.toString());
                ret.add(basicNameValuePair);
            }
        }
        return ret;
    }

    private String buildGetData(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder();
        if (params != null && params.size() != 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key == null || key.trim().length() == 0 || value == null) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append("&");
                } else {
                    builder.append("?");
                }
                builder.append(key).append("=").append(value);
            }
        }
        return builder.toString();
    }

    public void get(String address, Map<String, Object> params, Map<String, String> _headers,
        AbstractFutureCallback callback) {
        StopWatch stopWatch = new StopWatch("get=" + address, true);
        String paramsStr = buildGetData(params);
        HttpGet httpGet = new HttpGet(address + paramsStr);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpGet.setHeader(k, v);
                    }
                });
            }
            stopWatch.stop();
            this.closeableHttpAsyncClient.execute(httpGet, callback);
            stopWatch.stop();
        } finally {
            stopWatch.log();
        }
    }

    public Future<HttpResponse> get(String address, Map<String, Object> params, Map<String, String> _headers) {
        StopWatch stopWatch = new StopWatch("get=" + address, true);
        String paramsStr = buildGetData(params);
        HttpGet httpGet = new HttpGet(address + paramsStr);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpGet.setHeader(k, v);
                    }
                });
            }
            stopWatch.stop();
            return this.closeableHttpAsyncClient.execute(httpGet, new DefaultFutureCallback());
        } finally {
            stopWatch.log();
        }
    }

    public void post(String address, Map<String, Object> params, Map<String, String> _headers,
        AbstractFutureCallback callback) throws IOException {
        List<NameValuePair> data = buildPostData(params);
        postInner(address, new UrlEncodedFormEntity(data, "UTF-8"), _headers, callback);
    }

    public void postInner(String address, HttpEntity httpEntity, Map<String, String> _headers,
        AbstractFutureCallback callback) {
        StopWatch stopWatch = new StopWatch("post=" + address, true);
        HttpPost httpPost = new HttpPost(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPost.setHeader(k, v);
                    }
                });
            }
            httpPost.setEntity(httpEntity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            this.closeableHttpAsyncClient.execute(httpPost, callback);
            stopWatch.stop();
        } finally {
            stopWatch.log();
        }
    }

    public void postJson(String address, String json, Map<String, String> _headers, AbstractFutureCallback callback)
        throws IOException {
        postJsonInner(address, new StringEntity(json, "UTF-8"), _headers, callback);
    }

    public void postJsonInner(String address, HttpEntity httpEntity, Map<String, String> _headers,
        AbstractFutureCallback callback) {
        StopWatch stopWatch = new StopWatch("postJson=" + address, true);
        HttpPost httpPost = new HttpPost(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPost.setHeader(k, v);
                    }
                });
            }
            httpPost.setEntity(httpEntity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
            stopWatch.stop();
            this.closeableHttpAsyncClient.execute(httpPost, callback);
            stopWatch.stop();
        } finally {
            stopWatch.log();
        }
    }

    public Future<HttpResponse> post(String address, Map<String, Object> params, Map<String, String> _headers)
        throws IOException {
        List<NameValuePair> data = buildPostData(params);
        return postInner(address, new UrlEncodedFormEntity(data, "UTF-8"), _headers);
    }

    public Future<HttpResponse> postInner(String address, HttpEntity httpEntity, Map<String, String> _headers) {
        StopWatch stopWatch = new StopWatch("post=" + address, true);
        HttpPost httpPost = new HttpPost(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPost.setHeader(k, v);
                    }
                });
            }
            httpPost.setEntity(httpEntity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            return this.closeableHttpAsyncClient.execute(httpPost, new DefaultFutureCallback());
        } finally {
            stopWatch.log();
        }
    }

    public Future<HttpResponse> postJson(String address, String json, Map<String, String> _headers) throws IOException {
        return postJsonInner(address, new StringEntity(json, "UTF-8"), _headers);
    }

    public Future<HttpResponse> postJsonInner(String address, HttpEntity httpEntity, Map<String, String> _headers) {
        StopWatch stopWatch = new StopWatch("postJson=" + address, true);
        HttpPost httpPost = new HttpPost(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPost.setHeader(k, v);
                    }
                });
            }
            httpPost.setEntity(httpEntity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
            stopWatch.stop();
            return this.closeableHttpAsyncClient.execute(httpPost, new DefaultFutureCallback());
        } finally {
            stopWatch.log();
        }
    }
}
