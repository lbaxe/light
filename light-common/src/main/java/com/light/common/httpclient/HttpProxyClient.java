package com.light.common.httpclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Http请求的工具类
 */
public class HttpProxyClient {

    private CloseableHttpClient closeableHttpClient;

    public HttpProxyClient(CloseableHttpClient closeableHttpClient) {
        this.closeableHttpClient = closeableHttpClient;
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

    public String get(String address, Map<String, Object> params) throws IOException {
        return get(address, params);
    }

    public String get(String address, Map<String, Object> params, Map<String, String> _headers) throws IOException {
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
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpGet);
            stopWatch.stop();
            return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } finally {
            httpGet.abort();
            stopWatch.log();
        }
    }

    public String post(String address, Map<String, Object> params) throws IOException {
        return this.post(address, params, (Map<String, String>)null);
    }

    public String post(String address, Map<String, Object> params, Map<String, String> _headers) throws IOException {
        List<NameValuePair> data = buildPostData(params);
        return post(address, new UrlEncodedFormEntity(data, "UTF-8"), _headers);
    }

    public String post(String address, HttpEntity entity, Map<String, String> _headers) throws IOException {
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
            httpPost.setEntity(entity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpPost);
            stopWatch.stop();
            return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } finally {
            httpPost.abort();
            stopWatch.log();
        }
    }

    public String postJson(String address, String json) throws IOException {
        return this.postJson(address, json, (Map<String, String>)null);
    }

    public String postJson(String address, String json, Map<String, String> _headers) throws IOException {
        return postJson(address, new StringEntity(json, "UTF-8"), _headers);
    }

    public String postJson(String address, HttpEntity entity, Map<String, String> _headers) throws IOException {
        HttpResponse httpResponse = postJsonInner(address, entity, _headers);
        return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
    }

    public HttpResponse postJsonInner(String address, HttpEntity entity, Map<String, String> _headers)
        throws IOException {
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
            httpPost.setEntity(entity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpPost);
            stopWatch.stop();
            return httpResponse;
        } finally {
            // httpPost.abort();
            stopWatch.log();
        }
    }

    public String put(String address, Map<String, Object> params) throws IOException {
        return this.put(address, params, (Map<String, String>)null);
    }

    public String put(String address, Map<String, Object> params, Map<String, String> _headers) throws IOException {
        StopWatch stopWatch = new StopWatch("HttpSendClient", true);
        HttpPut httpPut = new HttpPut(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPut.setHeader(k, v);
                    }
                });
            }
            List<NameValuePair> data = buildPostData(params);
            httpPut.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
            httpPut.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpPut);
            stopWatch.stop();
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            stopWatch.log();
        }
    }

    public String delete(String address) throws IOException {
        return this.delete(address, (Map<String, String>)null);
    }

    public String delete(String address, Map<String, String> _headers) throws IOException {
        StopWatch stopWatch = new StopWatch("HttpSendClient", true);
        HttpDelete httpDelete = new HttpDelete(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpDelete.setHeader(k, v);
                    }
                });
            }
            httpDelete.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpDelete);
            stopWatch.stop();
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            stopWatch.log();
        }
    }

    public String putJson(String address, String json, Map<String, String> _headers) throws IOException {
        StopWatch stopWatch = new StopWatch("HttpSendClient", true);
        HttpPut httpPut = new HttpPut(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpPut.setHeader(k, v);
                    }
                });
            }
            httpPut.setEntity(new StringEntity(json, "UTF-8"));
            httpPut.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpPut);
            stopWatch.stop();
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            stopWatch.log();
        }
    }

    public String deleteJson(String address, Map<String, String> _headers) throws IOException {
        StopWatch stopWatch = new StopWatch("HttpSendClient", true);
        HttpDelete httpDelete = new HttpDelete(address);
        try {
            if (_headers != null && _headers.size() != 0) {
                _headers.forEach((k, v) -> {
                    if (k != null) {
                        httpDelete.setHeader(k, v);
                    }
                });
            }
            httpDelete.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
            stopWatch.stop();
            HttpResponse httpResponse = this.closeableHttpClient.execute(httpDelete);
            stopWatch.stop();
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            stopWatch.log();
        }
    }

    public <T> T get(String address, Map<String, Object> params, CallBack<T> callback) throws IOException {
        return this.get(address, params, null, callback);
    }

    public <T> T get(String address, Map<String, Object> params, Map<String, String> _headers, CallBack<T> callback)
        throws IOException {
        String ret = this.get(address, params, _headers);
        return callback.call(ret);
    }

    public <T> T post(String address, Map<String, Object> params, CallBack<T> callback) throws IOException {
        return this.post(address, params, null, callback);
    }

    public <T> T post(String address, Map<String, Object> params, Map<String, String> _headers, CallBack<T> callback)
        throws IOException {
        String ret = post(address, params, _headers);
        return callback.call(ret);
    }

    public <T> T postJson(String address, String json, CallBack<T> callback) throws IOException {
        return this.postJson(address, json, null, callback);
    }

    public <T> T postJson(String address, String json, Map<String, String> _headers, CallBack<T> callback)
        throws IOException {
        String ret = postJson(address, json, _headers);
        return callback.call(ret);
    }

    public <T> T put(String address, Map<String, Object> params, CallBack<T> callback) throws IOException {
        return this.put(address, params, null, callback);
    }

    public <T> T put(String address, Map<String, Object> params, Map<String, String> _headers, CallBack<T> callback)
        throws IOException {
        String ret = this.put(address, params, _headers);
        return callback.call(ret);
    }

    public <T> T putJson(String address, String json, CallBack<T> callback) throws IOException {
        return this.putJson(address, json, null, callback);
    }

    public <T> T putJson(String address, String json, Map<String, String> _headers, CallBack<T> callback)
        throws IOException {
        String ret = this.putJson(address, json, _headers);
        return callback.call(ret);
    }

    public <T> T delete(String address, CallBack<T> callback) throws IOException {
        return this.delete(address, null, callback);
    }

    public <T> T delete(String address, Map<String, String> _headers, CallBack<T> callback) throws IOException {
        String ret = this.delete(address, _headers);
        return callback.call(ret);
    }

    public <T> T deleteJson(String address, CallBack<T> callback) throws IOException {
        return this.deleteJson(address, null, callback);
    }

    public <T> T deleteJson(String address, Map<String, String> _headers, CallBack<T> callback) throws IOException {
        String ret = this.deleteJson(address, _headers);
        return callback.call(ret);
    }
}
