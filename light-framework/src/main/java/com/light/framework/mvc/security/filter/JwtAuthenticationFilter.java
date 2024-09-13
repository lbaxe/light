package com.light.framework.mvc.security.filter;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.light.core.conts.Const;
import com.light.framework.mvc.security.token.JwtAuthenticationToken;
import com.light.framework.mvc.util.TokenUtil;

public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final RequestMatcher notOauthMatcher =
        new NegatedRequestMatcher(new AntPathRequestMatcher("/oauth2/**"));
    private static final RequestMatcher notLoginMatcher =
        new NegatedRequestMatcher(new AntPathRequestMatcher("/**login**"));
    private static final RequestMatcher notRpcMatcher =
        new NegatedRequestMatcher(new AntPathRequestMatcher("/" + Const.NAMESPACE_API + "/**"));
    private static final RequestMatcher DEFAULT_REQUEST_MATCHER =
        new AndRequestMatcher(Arrays.asList(notOauthMatcher, notLoginMatcher, notRpcMatcher));

    public JwtAuthenticationFilter() {
        super(DEFAULT_REQUEST_MATCHER);
    }

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_REQUEST_MATCHER, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        final String token = TokenUtil.getToken(request);
        if (StringUtils.isBlank(token) || !token.startsWith("Bearer ")) {
            throw new AuthenticationServiceException("Authentication token not supported: " + token);
        }
        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(token, null);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * Provided so that subclasses may configure what is put into the authentication request's details property.
     * 
     * @param request that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details set
     */
    protected void setDetails(HttpServletRequest request, JwtAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}