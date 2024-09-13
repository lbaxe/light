package com.light.framework.mvc.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.framework.mvc.DownloadContentType;
import com.light.framework.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.core.log.DebugLogger;
import com.light.framework.mvc.util.ServletUtil;

public class BaseController {
    protected static final DebugLogger debugLogger = DebugLogger.getInstance();
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected final void download(byte[] bytes) throws IOException {
        download(bytes, RandomUtil.randomLetterOrDigit(10), DownloadContentType.DEFAULT.getValue(), false);
    }

    protected final void download(byte[] bytes, String filename) throws IOException {
        download(bytes, filename, DownloadContentType.DEFAULT.getValue(), false);
    }

    protected final void download(byte[] bytes, String filename, DownloadContentType contentType) throws IOException {
        download(bytes, filename, contentType.getValue(), false);
    }

    protected final void downloadCrossDomain(byte[] bytes, boolean cors) throws IOException {
        download(bytes, RandomUtil.randomLetterOrDigit(10), DownloadContentType.DEFAULT.getValue(), cors);
    }

    protected final void downloadCrossDomain(byte[] bytes, String filename, boolean cors) throws IOException {
        download(bytes, filename, DownloadContentType.DEFAULT.getValue(), cors);
    }

    protected final void downloadCrossDomain(byte[] bytes, String filename, DownloadContentType contentType,
        boolean cors) throws IOException {
        download(bytes, filename, contentType.getValue(), cors);
    }

    protected final void download(byte[] bytes, String filename, String contentType, boolean cors) throws IOException {
        HttpServletResponse response = getResponse();
        HttpServletRequest request = getRequest();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && (userAgent.toLowerCase().indexOf("msie") > 0
            || userAgent.toLowerCase().indexOf("rv:11.0") > 0 || userAgent.toLowerCase().indexOf("edge") > 0)) {
            filename = URLEncoder.encode(filename, "UTF-8");
            filename = filename.replaceAll("\\+", "%20");
        } else {
            filename = new String(filename.getBytes("UTF-8"), "ISO_8859_1");
        }
        response.setContentType(contentType);
        response.setHeader("Access-Control-Expose-Headers", "Content-disposition");
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        response.setHeader("Content-Length", (new StringBuilder(String.valueOf(bytes.length))).toString());
        // 防止编码时强行调用response.reset(),清空全局cors设置
        if (cors) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());
            bos.write(bytes);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException iOException) {
                }
            }
        }
    }

    protected final void download(InputStream is) throws IOException {
        download(is, RandomUtil.randomLetterOrDigit(10), DownloadContentType.DEFAULT.getValue(), false);
    }

    protected final void download(InputStream is, String filename) throws IOException {
        download(is, filename, DownloadContentType.DEFAULT.getValue(), false);
    }

    protected final void download(InputStream is, String filename, DownloadContentType contentType) throws IOException {
        download(is, filename, contentType.getValue(), false);
    }

    protected final void downloadCrossDomain(InputStream is, boolean cors) throws IOException {
        download(is, RandomUtil.randomLetterOrDigit(10), DownloadContentType.DEFAULT.getValue(), cors);
    }

    protected final void downloadCrossDomain(InputStream is, String filename, boolean cors) throws IOException {
        download(is, filename, DownloadContentType.DEFAULT.getValue(), cors);
    }

    protected final void downloadCrossDomain(InputStream is, String filename, DownloadContentType contentType,
        boolean cors) throws IOException {
        download(is, filename, contentType.getValue(), cors);
    }

    protected final void download(InputStream is, String filename, String contentType, boolean cors)
        throws IOException {
        HttpServletResponse response = getResponse();
        HttpServletRequest request = getRequest();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && (userAgent.toLowerCase().indexOf("msie") > 0
            || userAgent.toLowerCase().indexOf("rv:11.0") > 0 || userAgent.toLowerCase().indexOf("edge") > 0)) {
            filename = URLEncoder.encode(filename, "UTF-8");
            filename = filename.replaceAll("\\+", "%20");
        } else {
            filename = new String(filename.getBytes("UTF-8"), "ISO_8859_1");
        }
        response.setContentType(contentType);
        response.setHeader("Access-Control-Expose-Headers", "Content-disposition");
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        // 防止编码时强行调用response.reset(),清空全局cors设置
        if (cors) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }

        ServletOutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        try {
            while (-1 != (n = is.read(buffer)))
                outputStream.write(buffer, 0, n);
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException iOException) {
                }
        }
    }

    private HttpServletRequest getRequest() {
        return ServletUtil.getRequest();
    }

    private HttpServletResponse getResponse() {
        return ServletUtil.getResponse();
    }
}