package com.light.web.common.shiro.filter;

import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.light.core.exception.ServiceException;
import com.light.core.util.ThrowableUtil;
import com.light.framework.mvc.filter.shiro.ShiroProxyChainFilter;
import com.light.web.common.exception.EnumApiException;
import com.light.web.common.oauth.OAuth2AccessToken;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.service.OAuth2ClientService;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * login请求认证过滤器
 */
@Service("ApiFilter")
public class ApiFilter extends BasicHttpAuthenticationFilter implements ShiroProxyChainFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OAuth2ClientService oAuth2ClientService;

    @Override
    public String urlPattern() {
        return "/api/**";
    }

    @Override
    public int priority() {
        return 5;
    }

    /**
     * 是否允许访问
     * 
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    /**
     * 访问拒绝后如何处理
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loginAttempt = this.isLoginAttempt(request, response);
        if (loginAttempt) {
            return this.executeLogin(request, response);
        }
        throw new ServiceException(EnumApiException.UNKNOWN);

    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String accessToken = httpServletRequest.getHeader("access_token");
        return StringUtils.hasText(accessToken);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String uri = httpServletRequest.getRequestURI();

        String accessToken = httpServletRequest.getHeader("access_token");
        String sign = httpServletRequest.getHeader("sign");
        String timestamp = httpServletRequest.getHeader("timestamp");
        String realIp = httpServletRequest.getHeader("X-Real-IP");
        String forwardedIp = httpServletRequest.getHeader("X-Forwarded-For");
        String remoteAddr = httpServletRequest.getRemoteAddr();

        // access_token
        if (!StringUtils.hasText(accessToken)) {
            throw new ServiceException(EnumApiException.INVALID);
        }
        // sign
        if (!StringUtils.hasText(sign)) {
            throw new ServiceException(EnumApiException.INVALID);
        }
        // timestamp
        if (!StringUtils.hasText(timestamp)) {
            throw new ServiceException(EnumApiException.INVALID);
        }
        // 验证时间戳
        long ts = 0L;
        try {
            ts = Long.parseLong(timestamp);
        } catch (Exception ignored) {
        }
        boolean isLegalTimestamp = this.checkTimestamp(ts);
        if (!isLegalTimestamp) {
            throw new ServiceException(EnumApiException.INVALID);
        }
        // 验签
        boolean checkSign = this.checkSign(sign, httpServletRequest.getParameterMap(), ts);
        if (!checkSign) {
            throw new ServiceException(EnumApiException.INVALID);
        }

        OAuth2AuthorizedClient auth2AuthorizedClient =
            oAuth2ClientService.getOAuth2AuthorizedClientByAccessToken(accessToken);
        // 验证clientId
        if (auth2AuthorizedClient == null || !StringUtils.hasText(auth2AuthorizedClient.getClientId())) {
            throw new ServiceException(EnumApiException.APP_AUTH);
        }
        // 验证accessToken
        if (!accessToken.startsWith(TokenType.BEARER.toString())
            || accessToken.length() <= TokenType.BEARER.toString().length() + 1
            || !auth2AuthorizedClient.getAccessTokenValue()
                .equals(accessToken.substring(TokenType.BEARER.toString().length()))) {
            throw new ServiceException(EnumApiException.APP_AUTH);
        }
        // 验证accessToken有效期
        if (auth2AuthorizedClient.getAccessTokenExpiresAt().compareTo(new Date()) <= 0) {
            throw new ServiceException(EnumApiException.ACCESS_TOKEN_EXPIRED);
        }

        boolean isRateLimit = this.rateLimit(auth2AuthorizedClient.getClientId(), uri);
        if (isRateLimit) {
            throw new ServiceException(EnumApiException.THRESHOLD);
        }
        // 验证ip白名单
        if (!this.isWhiteIp((HttpServletRequest)request)) {
            throw new ServiceException(EnumApiException.IPLEGAL);
        }
        logger.info(uri + ",当前请求clientId=" + auth2AuthorizedClient.getClientId() + ",accessToken=" + accessToken
            + ",sign=" + sign + ",请求格式=" + request.getContentType() + " realIp:" + realIp + " forwardedIp:"
            + forwardedIp + " remoteAddr:" + remoteAddr);
        try {
            OAuth2AccessToken token = new OAuth2AccessToken(auth2AuthorizedClient.getClientId(),
                auth2AuthorizedClient.getAccessTokenValue(), TokenType.BEARER,
                auth2AuthorizedClient.getAccessTokenExpiresAt().getTime() / 1000,
                auth2AuthorizedClient.getRefreshTokenValue(), auth2AuthorizedClient.getAccessTokenScopes());
            Subject subject = this.getSubject(request, response);
            subject.login(token);
            return this.onLoginSuccess(token, subject, request, response);
        } catch (Exception e) {
            Throwable actual = ThrowableUtil.unwrapThrowable(e);
            if (actual instanceof ServiceException) {
                throw (ServiceException)e;
            } else if (actual.getCause() instanceof ServiceException) {
                throw (ServiceException)e.getCause();
            } else {
                throw new ServiceException(EnumApiException.UNKNOWN);
            }
        }
    }

    /**
     * 时间戳验证，防止ddos
     */
    private boolean checkTimestamp(long timestamp) {
        return timestamp != 0 && System.currentTimeMillis() - timestamp < 1000 * 60 * 5;
    }

    private boolean checkSign(String sign, Map<String, String[]> paramMap, long timestamp) {
        List<String> keyList = new ArrayList<>(paramMap.keySet());
        Collections.sort(keyList);
        StringBuilder builder = new StringBuilder();
        for (String key : keyList) {
            String[] valueArr = paramMap.get(key);
            // 重名字段只取第一个
            if (valueArr != null && valueArr.length > 0 && StringUtils.hasText(valueArr[0])) {
                builder.append(key).append(valueArr[0]);
            }
        }
        builder.append(timestamp);
        return this.checkSign(sign, builder.toString());
    }

    private boolean checkSign(String signData, String unsignData) {
        String newSignData = DigestUtil.md5Hex(unsignData);
        logger.info("unsignData=" + unsignData + ",newSignData=" + newSignData + ",第三方 signData=" + signData);
        return newSignData.equals(signData);
    }

    private boolean rateLimit(String clientId, String uri) {
        return false;
    }

    /**
     * 验证IP白名单
     */
    private boolean isWhiteIp(HttpServletRequest httpServletRequest) {
        return true;
    }
}
