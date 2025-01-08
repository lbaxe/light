package com.light.framework.mvc.filter.light.inner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.core.conts.Const;
import com.light.core.exception.ServiceException;
import com.light.framework.mvc.filter.light.GenericFilterBean;
import com.light.framework.mvc.util.RequestUtil;

public class RequestParamFilter extends GenericFilterBean {
    private static final Logger logger = LoggerFactory.getLogger(RequestParamFilter.class);
    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 50;
    protected static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<String> ORDERED_PARAMKEY = new LinkedHashSet<>();

    private boolean includeQueryString = false;

    private boolean includeClientInfo = false;

    private boolean includeHeaders = false;

    private boolean includePayload = false;
    @Nullable
    private Predicate<String> headerPredicate;

    private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

    public RequestParamFilter() {
        OBJECT_MAPPER.getSerializationConfig().with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        ORDERED_PARAMKEY.add(Const.TRACE_ID);
        ORDERED_PARAMKEY.add(Const.CLIENT_IP);
        ORDERED_PARAMKEY.add(Const.CURRENT_URL);
        ORDERED_PARAMKEY.add(Const.CURRENT_USER_ID);

        includeQueryString = true;
        includeClientInfo = true;
        includeHeaders = true;
        includePayload = true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        MediaType mediaType = this.supportMediaType(request);
        if (MediaType.APPLICATION_JSON == mediaType && !(request instanceof ContentCachingRequestWrapper)) {
            // request = new BodyReaderHttpServletRequestWrapper(request);
            request = new ContentCachingRequestWrapper(request);
        }

        Map<String, Object> data = getInput(request);
        if (data == null) {
            data = new HashMap<>();
        }

        if (data.size() > 1) {
            data = sortInputParams(data);
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put(Const.DATA, data);
        RequestUtil.setInput(request, input);

        filterChain.doFilter(request, response);
    }

    private Map<String, Object> getInput(HttpServletRequest request) throws ServiceException {
        Map<String, Object> input = null;
        MediaType mediaType = this.supportMediaType(request);
        if (mediaType == null) {
            logger.warn("unsupported Content-Type:" + request.getContentType() + ", ServletPath="
                + request.getServletPath() + ", Referer=" + request.getHeader("referer"));
            return input;
        }
        try {
            if (MediaType.APPLICATION_FORM_URLENCODED == mediaType) {
                input = getForm(request);
            } else if (MediaType.APPLICATION_JSON == mediaType) {
                input = getJson(request.getInputStream());
            } else if (MediaType.TEXT_HTML == mediaType) {
                // throw new RuntimeException("no support " + MediaType.TEXT_HTML_VALUE + " InputStream");
            } else if (MediaType.MULTIPART_FORM_DATA == mediaType) {
                input = getForm(request);
                // TODO luban
                // input.put("MULTIPART_FORMDATA", "");
            }
        } catch (Exception e) {
            logger.error("invalid request: " + createMessage(request, "[", "]"));
            if (e instanceof ServiceException) {
                throw (ServiceException)e;
            }
            throw new ServiceException("系统错误", e);
        }
        return input;
    }

    private Map<String, Object> getForm(HttpServletRequest req) {
        Set<Map.Entry<String, String[]>> set = req.getParameterMap().entrySet();
        Map<String, Object> inputMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : set) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (value == null || value.length == 0) {
                logger.debug("get(" + key + ") is null or empty.");
                continue;
            }
            if (value.length == 1) {
                inputMap.put(key, value[0]);
                continue;
            }
            inputMap.put(key, Arrays.asList(value));
        }
        return inputMap;
    }

    private Map<String, Object> getJson(InputStream inputStream) throws IOException {
        return getMap(OBJECT_MAPPER.readTree(inputStream));
    }

    private Map<String, Object> getMap(JsonNode node) {
        Map<String, Object> inputMap = new HashMap<>();
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode child = node.get(fieldName);
            if (child.isValueNode()) {
                inputMap.put(fieldName, child.asText());
                continue;
            }
            inputMap.put(fieldName, child);
        }
        return inputMap;
    }

    private Map<String, Object> sortInputParams(Map<String, Object> map) {
        Map<String, Object> sortMap = new LinkedHashMap<>();
        if (map != null && map.size() > 0) {
            for (String key : ORDERED_PARAMKEY) {
                Object value = map.get(key);
                if (value != null) {
                    sortMap.put(key, value);
                }
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (ORDERED_PARAMKEY.contains(entry.getKey())) {
                    continue;
                }
                sortMap.put(entry.getKey(), entry.getValue());
            }
        }
        return sortMap;
    }

    private String printHeader(HttpServletRequest request) {
        StringBuilder log = new StringBuilder();
        log.append("url=").append(request.getServletPath()).append(" | headers= ");
        Enumeration<?> headerNames = request.getHeaderNames();
        int i = 0;
        while (headerNames.hasMoreElements()) {
            String name = (String)headerNames.nextElement();
            if (i++ != 0) {
                log.append(", ");
            }
            log.append(name).append("=").append(request.getHeader(name));
        }
        return log.toString();
    }

    protected String createMessage(HttpServletRequest request, String prefix, String suffix) {
        StringBuilder msg = new StringBuilder();
        msg.append(prefix);
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (isIncludeQueryString()) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }

        if (isIncludeClientInfo()) {
            String client = request.getRemoteAddr();
            if (StringUtils.hasLength(client)) {
                msg.append(", client=").append(client);
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                msg.append(", session=").append(session.getId());
            }
            String user = request.getRemoteUser();
            if (user != null) {
                msg.append(", user=").append(user);
            }
        }

        if (isIncludeHeaders()) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            if (getHeaderPredicate() != null) {
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String header = names.nextElement();
                    if (!getHeaderPredicate().test(header)) {
                        headers.set(header, "masked");
                    }
                }
            }
            msg.append(", headers=").append(headers);
        }

        if (isIncludePayload()) {
            String payload = getMessagePayload(request);
            if (payload != null) {
                msg.append(", payload=").append(payload);
            }
        }

        msg.append(suffix);
        return msg.toString();
    }

    @Override
    protected String urlPattern() {
        return "/*";
    }

    public boolean isIncludeQueryString() {
        return includeQueryString;
    }

    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public boolean isIncludeClientInfo() {
        return includeClientInfo;
    }

    public void setIncludeClientInfo(boolean includeClientInfo) {
        this.includeClientInfo = includeClientInfo;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public boolean isIncludePayload() {
        return includePayload;
    }

    public void setIncludePayload(boolean includePayload) {
        this.includePayload = includePayload;
    }

    @Nullable
    public Predicate<String> getHeaderPredicate() {
        return headerPredicate;
    }

    public void setHeaderPredicate(@Nullable Predicate<String> headerPredicate) {
        this.headerPredicate = headerPredicate;
    }

    @Nullable
    protected String getMessagePayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, getMaxPayloadLength());
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    return "[unknown]";
                }
            }
        }
        return null;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public MediaType supportMediaType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null || contentType.trim().length() == 0
            || contentType.toLowerCase().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            return MediaType.APPLICATION_FORM_URLENCODED;
        }
        if (contentType.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            return MediaType.APPLICATION_JSON;
        }
        if (contentType.toLowerCase().startsWith(MediaType.TEXT_HTML_VALUE)) {
            return MediaType.TEXT_HTML;
        }
        if (contentType.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return MediaType.MULTIPART_FORM_DATA;
        }
        return null;
    }
}
