package com.light.framework.mvc.http;

import java.io.*;
import java.nio.charset.Charset;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = readBytes(request.getReader(), "utf-8");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return IOUtils.toBufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream inputStream = IOUtils.toBufferedInputStream(new ByteArrayInputStream(body));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }

    /**
     * 通过BufferedReader和字符编码集转换成byte数组
     * 
     * @param br
     * @param encoding
     * @return
     * @throws IOException
     */
    private byte[] readBytes(BufferedReader br, String encoding) throws IOException {
        String str = null, retStr = "";
        while ((str = br.readLine()) != null) {
            retStr += str;
        }
        if (StringUtils.isNotBlank(retStr)) {
            return retStr.getBytes(Charset.forName(encoding));
        }
        return new byte[0];
    }

}