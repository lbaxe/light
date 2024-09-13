package com.light.framework.mvc.filter.global;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.core.conts.Const;
import com.light.core.exception.ServiceException;
import com.light.core.log.DebugLogger;
import com.light.framework.mvc.filter.GenericFilterBean;
import com.light.framework.mvc.http.BodyReaderHttpServletRequestWrapper;
import com.light.framework.mvc.util.RequestUtil;

public class RequestParamFilter extends GenericFilterBean {
    private static final Logger logger = LoggerFactory.getLogger(RequestParamFilter.class);
    protected static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<String> ORDERED_PARAMKEY = new LinkedHashSet<>();

    public RequestParamFilter() {
        OBJECT_MAPPER.getSerializationConfig().with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        ORDERED_PARAMKEY.add(Const.TRACE_ID);
        ORDERED_PARAMKEY.add(Const.CLIENT_IP);
        ORDERED_PARAMKEY.add(Const.CURRENT_URL);
        ORDERED_PARAMKEY.add(Const.CURRENT_USER_ID);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        MediaType mediaType = this.supportMediaType(request);
        if (MediaType.APPLICATION_JSON == mediaType) {
            request = new BodyReaderHttpServletRequestWrapper(request);
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
            DebugLogger.getInstance().log("unsupported Content-Type:" + request.getContentType() + ", ServletPath="
                + request.getServletPath() + ", Referer=" + request.getHeader("referer"));
            return input;
        }
        try {
            if (MediaType.APPLICATION_FORM_URLENCODED == mediaType) {
                input = getForm(request);
            } else if (MediaType.APPLICATION_JSON == mediaType) {
                input = getJson(request.getInputStream());
            } else if (MediaType.TEXT_HTML == mediaType) {
                throw new RuntimeException("no support " + MediaType.TEXT_HTML_VALUE + " InputStream");
            } else if (MediaType.MULTIPART_FORM_DATA == mediaType) {
                input = getForm(request);
                // TODO luban
                // input.put("MULTIPART_FORMDATA", "");
            }
        } catch (Exception e) {
            DebugLogger.getInstance().log("invalid request: " + printHeader(request));
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

    @Override
    protected String urlPattern() {
        return "/*";
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
